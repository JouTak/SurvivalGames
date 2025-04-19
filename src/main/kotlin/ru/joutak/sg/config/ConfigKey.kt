package ru.joutak.sg.config

interface ConfigKey<T : Any> {
    val path: String
    val value: T

    fun parse(input: String): T?
}
