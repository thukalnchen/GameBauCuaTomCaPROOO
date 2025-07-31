# PHÂN TÍCH LẬP TRÌNH CALL API ĐỂ THANH TOÁN TRÊN PAYPAL

## 1. TỔNG QUAN HỆ THỐNG THANH TOÁN PAYPAL

Hệ thống thanh toán PayPal trong ứng dụng game Bầu Cua Tôm Cá được thiết kế theo mô hình tích hợp API bên thứ ba, cho phép người chơi nạp tiền vào tài khoản thông qua cổng thanh toán quốc tế. Hệ thống sử dụng PayPal REST API để xử lý các giao dịch thanh toán một cách an toàn và đáng tin cậy.

### 1.1. Kiến trúc tổng thể
Hệ thống bao gồm các thành phần chính: PayPalConfig để cấu hình kết nối API, PayPalService xử lý logic thanh toán, WebServer làm server callback để nhận kết quả từ PayPal, NapTienScene cung cấp giao diện người dùng, và NapTienLog lưu trữ lịch sử giao dịch.

### 1.2. Luồng thanh toán
Quy trình thanh toán bắt đầu khi người dùng chọn mệnh giá nạp tiền từ giao diện. Hệ thống tạo một giao dịch thanh toán thông qua PayPal API và chuyển hướng người dùng đến trang thanh toán của PayPal. Sau khi hoàn tất thanh toán, PayPal gửi callback về server để xác nhận và cập nhật số dư tài khoản người chơi.

## 2. PHÂN TÍCH CHI TIẾT CÁC THÀNH PHẦN

### 2.1. Cấu hình kết nối PayPal (PayPalConfig.java)

Lớp PayPalConfig chịu trách nhiệm quản lý thông tin xác thực và cấu hình kết nối với PayPal API. Hệ thống sử dụng sandbox mode để test trong môi trường phát triển, với Client ID và Client Secret được lấy từ PayPal Developer Dashboard. Phương thức getAPIContext() tạo và trả về một APIContext object để sử dụng cho các cuộc gọi API.

### 2.2. Service xử lý thanh toán (PayPalService.java)

PayPalService là lớp core xử lý tất cả logic liên quan đến thanh toán PayPal. Phương thức createPayment() nhận các tham số như số tiền, loại tiền tệ, mô tả giao dịch và URL callback để tạo một giao dịch thanh toán mới. Service này tạo các object Amount, Transaction, Payer và Payment theo đúng cấu trúc yêu cầu của PayPal API.

Phương thức executePayment() được sử dụng để xác nhận giao dịch sau khi người dùng hoàn tất thanh toán trên PayPal. Nó nhận paymentId và payerId từ callback để thực hiện việc capture tiền.

### 2.3. Web Server Callback (WebServer.java)

WebServer được khởi động trên cổng 4567 để lắng nghe các callback từ PayPal. Server này xử lý hai endpoint chính: /success cho các giao dịch thành công và /cancel cho các giao dịch bị hủy.

Khi nhận được callback thành công, server parse thông tin từ URL query parameters để lấy paymentId, payerId và số tiền. Sau đó gọi PayPalService.executePayment() để xác nhận giao dịch với PayPal, cập nhật số dư người dùng trong cơ sở dữ liệu và lưu log giao dịch.

### 2.4. Giao diện người dùng (NapTienScene.java)

NapTienScene cung cấp giao diện người dùng thân thiện cho việc nạp tiền. Giao diện bao gồm logo PayPal, các tùy chọn mệnh giá (5, 10, 20 USD) được thiết kế dạng card, và nút "Nạp tiền ngay".

Khi người dùng chọn mệnh giá và nhấn nút thanh toán, hệ thống gọi PayPalService.createPayment() để tạo giao dịch và mở trình duyệt đến trang thanh toán PayPal. Đồng thời, một thread riêng được tạo để đợi callback từ server và cập nhật giao diện khi thanh toán hoàn tất.

### 2.5. Model dữ liệu (NapTienLog.java)

NapTienLog là model đại diện cho một bản ghi nạp tiền trong hệ thống. Nó chứa các thông tin cơ bản như id, userId, số tiền nạp và thời gian tạo giao dịch. Model này được sử dụng để lưu trữ lịch sử nạp tiền vào cơ sở dữ liệu.

## 3. PHÂN TÍCH KỸ THUẬT

### 3.1. Bảo mật
Hệ thống sử dụng sandbox mode để đảm bảo an toàn trong quá trình phát triển và test. Tất cả giao tiếp với PayPal đều thông qua HTTPS, và hệ thống validate paymentId và payerId từ PayPal để tránh gian lận.

### 3.2. Xử lý lỗi
Hệ thống implement try-catch blocks để bắt các exception từ PayPal API, kiểm tra null cho dữ liệu đầu vào, và cung cấp feedback phù hợp cho người dùng khi có lỗi xảy ra.

### 3.3. Đồng bộ hóa
Để xử lý callback từ PayPal, hệ thống sử dụng thread riêng để đợi kết quả thanh toán. Flag WebServer.paymentDone được sử dụng để đồng bộ trạng thái giữa các thread, và Platform.runLater đảm bảo cập nhật UI thread một cách an toàn.

### 3.4. Tỷ giá hối đoái
Hệ thống sử dụng tỷ giá cố định 1 USD = 24,000 VND để chuyển đổi số tiền. Điều này đơn giản hóa việc tính toán nhưng có thể không phản ánh tỷ giá thực tế trên thị trường.

## 4. ĐÁNH GIÁ VÀ ĐỀ XUẤT CẢI TIẾN

### 4.1. Ưu điểm
Hệ thống có kiến trúc rõ ràng với việc tách biệt các thành phần, xử lý lỗi tốt, giao diện người dùng thân thiện và có khả năng lưu trữ lịch sử giao dịch đầy đủ.

### 4.2. Hạn chế
Tỷ giá hối đoái cố định có thể không chính xác, hệ thống phụ thuộc vào ngrok để expose local server, và chỉ có thể xử lý một giao dịch tại một thời điểm.

### 4.3. Đề xuất cải tiến
Cần tích hợp API tỷ giá thực tế, sử dụng webhook thay vì polling, implement retry mechanism, thêm validation cho số tiền và sử dụng connection pooling cho database.

## 5. KẾT LUẬN

Hệ thống thanh toán PayPal được thiết kế theo kiến trúc module hóa, với việc tách biệt rõ ràng giữa các thành phần. Việc sử dụng PayPal REST API cho phép tích hợp thanh toán một cách an toàn và đáng tin cậy. Tuy nhiên, cần cải thiện một số điểm về tỷ giá hối đoái và khả năng mở rộng để đáp ứng nhu cầu thực tế. 