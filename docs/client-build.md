# 📱 Сборка Android приложения

## Предварительные требования

- Android Studio Arctic Fox или выше
- Android SDK 33
- Java 8 или Kotlin

## Установка

1. Откройте Android Studio
2. Выберите "Open Project"
3. Укажите папку `client/`
4. Дождитесь синхронизации Gradle

## Настройка

### Важно! Измените IP адрес сервера:

В файле `ApiClient.kt` замените BASE_URL на IP вашего сервера:

```kotlin
private const val BASE_URL = "http://ВАШ_IP:3000"