package com.example.profolio.document;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.profolio.R;
import com.example.profolio.printlayout.PrintViewActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DocumentPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocumentPageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters

    AppCompatButton btn_generate;
    private static final int REQUEST_CODE = 1232;
    LinearLayout card, header_title;
    private String mParam1;
    private String mParam2;

    public DocumentPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DocumentPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DocumentPageFragment newInstance(String param1, String param2) {
        DocumentPageFragment fragment = new DocumentPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_document_page, container, false);

        btn_generate = view.findViewById(R.id.btn_generate);
        card = view.findViewById(R.id.cardCreateCV);
        header_title = view.findViewById(R.id.header_title);

        askPermissions();

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        card.setAnimation(animation);
        header_title.setAnimation(animation);

        btn_generate.setOnClickListener(v -> {
            Intent next = new Intent(getContext(), PrintViewActivity.class);
            startActivity(next);
        });
        return view;
    }

    private void createPDF() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.activity_print_view, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().getDisplay().getRealMetrics(displayMetrics);
        } else {
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        view.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY));

        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();

        Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        view.layout(0, 0, viewWidth, viewHeight);
        view.draw(canvas);

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas pageCanvas = page.getCanvas();
        pageCanvas.drawBitmap(bitmap, 0, 0, null);

        pdfDocument.finishPage(page);

        String baseFilename = "CV";
        String filename = generateUniqueFilename(baseFilename, "pdf");
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, filename);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            Toast.makeText(getContext(), "Export Success", Toast.LENGTH_SHORT).show();
            openPDFFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openPDFFile(File file) {
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No PDF viewer app found", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateUniqueFilename(String baseFilename, String extension) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filename = baseFilename + "." + extension;
        int counter = 1;

        while (new File(downloadsDir, filename).exists()) {
            filename = baseFilename + "(" + counter + ")." + extension;
            counter++;
        }

        return filename;
    }
    private void askPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }
}