package org.example;

import org.springframework.lang.NonNull;

import java.io.File;
import java.util.List;

public record Mail (
        @NonNull
        List<String> recipients,
        @NonNull
        String subject,
        @NonNull
        String body,
        File... attachedFiles){}
