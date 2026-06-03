import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class DioClient {
  static const String baseUrl = 'http://10.0.2.2:8080/api/v1';
  // Use 'http://localhost:8080/api/v1' on iOS simulator or physical device

  static const _storage = FlutterSecureStorage();

  static Dio get instance {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 15),
        headers: {'Content-Type': 'application/json'},
      ),
    );

    dio.interceptors.add(
      InterceptorsWrapper(
        // Attach the stored access token to every request
        onRequest: (options, handler) async {
          final token = await _storage.read(key: 'access_token');
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          handler.next(options);
        },

        // Retry logic: only on 401; never on 403, 404, 422, 429, 5xx, etc.
        onError: (DioException error, handler) async {
          final statusCode = error.response?.statusCode;

          // Fix: 403/404/422/5xx → pass straight through, NO retry
          if (statusCode != 401) {
            return handler.next(error);
          }

          // Fix: prevent infinite retry loop — if this request was already
          // retried once, clear credentials and propagate the error
          if (error.requestOptions.extra['_retried'] == true) {
            await _storage.deleteAll();
            return handler.reject(error);
          }

          // Attempt token refresh
          try {
            final refreshToken = await _storage.read(key: 'refresh_token');
            if (refreshToken == null) {
              await _storage.deleteAll();
              return handler.reject(error);
            }

            final refreshDio = Dio(BaseOptions(
              baseUrl: baseUrl,
              connectTimeout: const Duration(seconds: 5),
              receiveTimeout: const Duration(seconds: 5),
            ));

            final refreshRes = await refreshDio.post(
              '/auth/refresh',
              data: {'refreshToken': refreshToken},
            );

            final newToken = refreshRes.data['data']?['accessToken'] as String?;
            if (newToken == null) {
              await _storage.deleteAll();
              return handler.reject(error);
            }

            await _storage.write(key: 'access_token', value: newToken);

            // Retry the original request once, marked to avoid another loop
            final retryOptions = error.requestOptions;
            retryOptions.headers['Authorization'] = 'Bearer $newToken';
            retryOptions.extra['_retried'] = true;

            final retryResponse = await Dio(
              BaseOptions(
                baseUrl: baseUrl,
                connectTimeout: const Duration(seconds: 10),
                receiveTimeout: const Duration(seconds: 15),
              ),
            ).fetch(retryOptions);

            return handler.resolve(retryResponse);
          } catch (_) {
            // Refresh failed: clear all credentials
            // The UI layer (e.g., authNotifierProvider) should detect the
            // missing token on next request and redirect to login.
            await _storage.deleteAll();
            return handler.reject(error);
          }
        },
      ),
    );

    return dio;
  }
}
