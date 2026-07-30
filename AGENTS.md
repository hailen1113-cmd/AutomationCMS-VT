# Quy ước quản lý testcase

Áp dụng cho toàn bộ mã nguồn kiểm thử trong repository này:

- Mỗi method mới có `@Test` phải được khai báo trước trong file `src/test/java/com/vuatho/testcases/*TestCases.java` đúng module.
- ID testcase phải cố định và tăng tuần tự theo module, ví dụ `EKYC-024`, `DASH-027`, `UNI-CAT-010`.
- Không đổi số, tái sử dụng hoặc đánh lại ID của testcase đã tồn tại.
- Tên testcase trong catalog phải viết bằng tiếng Việt có dấu, ngắn gọn và mô tả đúng kết quả cần kiểm tra.
- `@Test(description = ...)` phải tham chiếu constant từ catalog, ví dụ `EkycTestCases.EKYC_024`.
- Không viết trực tiếp chuỗi mô tả trong `@Test`, không dùng reflection để sinh danh sách, không dùng CRC32 và không sinh ID ngẫu nhiên.
- Khi có module mới, tạo file `<Module>TestCases.java` trong package `com.vuatho.testcases`.
- Việc thêm catalog hoặc ID không được thay đổi flow hay thân method testcase hiện tại.

Trước khi bàn giao, phải kiểm tra số lượng `@Test`, constant và tham chiếu catalog khớp nhau, sau đó chạy `mvn -q -DskipTests test-compile`.
