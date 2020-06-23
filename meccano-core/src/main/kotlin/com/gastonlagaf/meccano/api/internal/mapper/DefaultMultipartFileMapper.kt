package com.gastonlagaf.meccano.api.internal.mapper

import com.gastonlagaf.meccano.annotation.MultipartFile
import com.gastonlagaf.meccano.api.HttpRequestMapper
import com.gastonlagaf.meccano.api.internal.CaretPosition
import com.gastonlagaf.meccano.api.internal.HttpRequestParser
import com.gastonlagaf.meccano.api.internal.support.HandlerMethodParameter
import com.gastonlagaf.meccano.api.internal.support.HttpHandlerMetaInfo
import com.gastonlagaf.meccano.api.internal.support.RequestContext
import com.gastonlagaf.meccano.api.support.HttpHeaderTitles
import com.gastonlagaf.meccano.api.support.InMemoryFile
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

class DefaultMultipartFileMapper : HttpRequestMapper {

    override fun getSupportedAnnotation(): KClass<out Annotation> = MultipartFile::class

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is MultipartFile } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any? {
        val multipartContent = context.request.getContentAsString()
        val annotation = parameter.annotation!! as MultipartFile

        val boundary = context.request.getHeader(HttpHeaderTitles.contentType.uppercasedValue)
            ?.let { HttpRequestParser(it, CaretPosition.HEADERS).boundary() }
            ?: throw IllegalArgumentException("Content-Type spec not found for multipart request")
        val splitBody = multipartContent.split(boundary)
        val filesList = splitBody.subList(1, splitBody.lastIndex)
        val files = mutableListOf<InMemoryFile>()
        filesList.forEach {
            val file = HttpRequestParser(it, CaretPosition.BODY).multipartFile()
            val extension = extractExtension(file.name)
            if (annotation.acceptExtensions.isNotEmpty() && !annotation.acceptExtensions.contains(extension)) {
                throw IllegalArgumentException("Unsupported file type with extension $extension")
            }
            files.add(file)
        }
        return if (parameter.clazz.isArray) {
            return files.toTypedArray()
        } else {
            files.first()
        }
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is MultipartFile }
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

    private fun extractExtension(fileName: String): String = fileName.substring(fileName.lastIndexOf('.') + 1)

}