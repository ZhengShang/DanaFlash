package com.ecreditpal.danaflash.model

data class FaqRes(
    val list: List<Faq>?
) {
    data class Faq(
        val content: String?, // Butuh Modal merupakan platform atau aplikasi pembanding produk pinjaman yang ada di Indonesia. Yang mana bertujuan untuk memudahkan pengguna dalam memilih produk pinjaman yang sesuai dengan kebutuhan melalui beberapa perbandingan seperti “Jumlah Tertinggi”, “Bunga Terendah”, “Cair Tercepat” “Produk Terbaru”, “Produk Rekomendasi” dsb. Butuh Modal juga dapat dianggap sebagai marketplace pinjaman karena semua produk pinjaman yang terdaftar dan di awasi oleh OJK ditampilkan didalam aplikasi ini. Sehingga pengguna tidak perlu khawatir mengajukan pinjaman melalui rekomendasi Butuh Modal. Selain itu, keunggulan Butuh Modal adalah menyediakan informasi yang sangat lengkap, jelas, dan akurat, yang tertampil dalam fitur-fitur didalam aplikasi Butuh Modal seperti “Tips dan Trik Supaya Pinjaman Cepat Disetujui” , “Kalkulator Bunga” dsb. Butuh Modal juga sering memberikan promo-promo menarik berhadiah secara berkala.
        val title: String? // Apa itu Butuh Modal ?
    )
}