package com.gastonlagaf.meccano.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MultipartFile(
    val acceptExtensions: Array<String> = []
)