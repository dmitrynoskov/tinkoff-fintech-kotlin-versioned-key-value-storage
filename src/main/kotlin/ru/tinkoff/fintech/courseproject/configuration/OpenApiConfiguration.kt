package ru.tinkoff.fintech.courseproject.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.parameters.HeaderParameter
import org.springdoc.core.GroupedOpenApi
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.tinkoff.fintech.courseproject.CourseProjectApplication


@Configuration
class OpenApiConfiguration {

    @Bean
    fun groupedOpenApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("public").packagesToScan(CourseProjectApplication::class.java.`package`.name).build()

    @Bean
    fun openAPI(): OpenAPI = OpenAPI().info(
        Info().title(CourseProjectApplication::class.java.simpleName)
    )

    @Bean
    fun consumerTypeHeaderOpenAPICustomiser(): OpenApiCustomiser? {
        return OpenApiCustomiser { openApi: OpenAPI ->
            openApi.paths.values.stream().flatMap { pathItem: PathItem ->
                pathItem.readOperations().stream()
            }
                .forEach { operation: Operation ->
                    operation.addParametersItem(
                        HeaderParameter().`$ref`("#/components/parameters/myConsumerTypeHeader")
                    )
                }
        }
    }
}
