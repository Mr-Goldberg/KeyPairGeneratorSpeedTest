package com.goldberg.keypairgeneratorspeedtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Handler mainThreadHandler = new Handler();

    @BindView(R.id.start_button)
    Button start_button;
    @BindView(R.id.time_text)
    TextView time_text;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        start_button.setOnClickListener(view ->
        {
            Log.d(TAG, "hang_button click");

            start_button.setEnabled(false);
            time_text.setText("Started");

            generateKeypairAsync();
        });
    }

    private void generateKeypairAsync()
    {
        // Main thread

        mainThreadHandler.post(() ->
        {
            AsyncTask.execute(() ->
            {
                // Background thread

                KeypairGenerationResult res = generateKeypair("alias_of_key");

                mainThreadHandler.post(() ->
                {
                    // Main thread

                    start_button.setEnabled(true);
                    time_text.setText(String.format("Time: %.2f", res.generationTimeMs / 1000.0));
                });
            });
        });
    }

    private static KeypairGenerationResult generateKeypair(String aliasOfKey)
    {
        Calendar dateValidFrom = Calendar.getInstance();
        Calendar dateValidTo = Calendar.getInstance();
        dateValidTo.add(Calendar.YEAR, 1);
        KeyGenParameterSpec specOfKey = new KeyGenParameterSpec.Builder(aliasOfKey, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(4096)
                .setKeyValidityStart(dateValidFrom.getTime())
                .setKeyValidityEnd(dateValidTo.getTime())
                .setCertificateSerialNumber(BigInteger.ONE)
                .setCertificateSubject(new X500Principal(String.format("CN=%s", "CertName")))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build();

        try
        {
            KeyPairGenerator generatorOfKeyPairs = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            generatorOfKeyPairs.initialize(specOfKey);

            Log.d(TAG, "start");

            long timeStart = System.currentTimeMillis();
            KeyPair keyPair = generatorOfKeyPairs.generateKeyPair();
            long timeDiff = System.currentTimeMillis() - timeStart;

            Log.d(TAG, "end, time ms: " + timeDiff);

            return new KeypairGenerationResult(keyPair, timeDiff);
        }
        catch (GeneralSecurityException ex)
        {
            Log.e(TAG, "Cannot generate wrapping keypair, exception: " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            return null;
        }
    }
}
