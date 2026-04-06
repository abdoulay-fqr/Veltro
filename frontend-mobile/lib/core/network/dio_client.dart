import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class DioClient {
  static const String baseUrl = 'http://localhost:8080/api/v1';
  static const _storage = FlutterSecureStorage();

  static Dio get instance {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 10),
        receiveTimeout: const Duration(seconds: 10),
        headers: {'Content-Type': 'application/json'},
      ),
    );

    // Request interceptor — attach access token
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await _storage.read(key: 'access_token');
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          handler.next(options);
        },
        onError: (error, handler) async {
          // Auto-refresh on 401
          if (error.response?.statusCode == 401) {
            try {
              final refreshToken = await _storage.read(key: 'refresh_token');
              final response = await Dio().post(
                '$baseUrl/auth/refresh',
                data: {'refreshToken': refreshToken},
              );
              final newToken = response.data['data']['accessToken'];
              await _storage.write(key: 'access_token', value: newToken);
              error.requestOptions.headers['Authorization'] = 'Bearer $newToken';
              return handler.resolve(await Dio().fetch(error.requestOptions));
            } catch (_) {
              await _storage.deleteAll();
            }
          }
          handler.next(error);
        },
      ),
    );

    return dio;
  }
}