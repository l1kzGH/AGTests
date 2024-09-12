package com.likz.agtests;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface INeuroApi {

    CompletableFuture<String> generateTestMethod(String methodData, String additionalData);
    CompletableFuture<List<String>> generateTestMethods(List<String> methodDataArr, String additionalData);

}
