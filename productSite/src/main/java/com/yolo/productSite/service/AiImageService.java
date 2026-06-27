package com.yolo.productSite.service;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

@Service
public class AiImageService {
    @Autowired
    private ImageModel imageModel;

    public byte[] generateImage(String imagePrompt) {
        StabilityAiImageOptions Options = StabilityAiImageOptions
                .builder()
                .N(1)
                // .responseFormat("url") // URL often fails or returns null for SDXL in some
                // contexts
                .build();
        ImageResponse response = imageModel.call(new ImagePrompt(imagePrompt, Options));

        String b64 = response.getResult().getOutput().getB64Json();
        if (b64 != null) {
            return Base64.getDecoder().decode(b64);
        }

        String imageUrl = response.getResult().getOutput().getUrl();
        if (imageUrl != null) {
            try {
                return new URL(imageUrl).openStream().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Failed to download image from URL", e);
            }
        }

        throw new RuntimeException("No image data returned from AI service");
    }
}
