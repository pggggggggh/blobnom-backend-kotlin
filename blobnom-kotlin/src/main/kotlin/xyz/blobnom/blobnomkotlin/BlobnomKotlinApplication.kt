package xyz.blobnom.blobnomkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class BlobnomKotlinApplication

fun main(args: Array<String>) {
    runApplication<BlobnomKotlinApplication>(*args)
}
