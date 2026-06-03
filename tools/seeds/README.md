# Veltro Seed Data

## Password Hash Generation

All seed accounts use the password: **Veltro@2024**

The BCrypt hashes in the SQL files are pre-computed with cost factor 10.
To regenerate (if Spring Security version changes), run:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashGen {
    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder(10);
        System.out.println(encoder.encode("Veltro@2024"));
    }
}
```

## Load Order

Run in this order (auth-service IDs must exist before user-service references them):

```bash
mysql -h 127.0.0.1 -P 3307 -u root -proot veltro_auth      < tools/seeds/auth.sql
mysql -h 127.0.0.1 -P 3308 -u root -proot veltro_user      < tools/seeds/user.sql
mysql -h 127.0.0.1 -P 3309 -u root -proot veltro_subscription < tools/seeds/subscription.sql
mysql -h 127.0.0.1 -P 3310 -u root -proot veltro_booking   < tools/seeds/booking.sql
mysql -h 127.0.0.1 -P 3311 -u root -proot veltro_activity  < tools/seeds/activity.sql
mysql -h 127.0.0.1 -P 3312 -u root -proot veltro_shop      < tools/seeds/shop.sql
```
