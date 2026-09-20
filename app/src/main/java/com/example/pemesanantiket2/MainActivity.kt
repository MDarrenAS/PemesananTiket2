package com.example.pemesanantiket2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pemesanantiket2.ui.theme.PemesananTiket2Theme
import kotlinx.coroutines.delay

/* ============================================================
 * STATUS PESANAN
 * Dipakai agar teks + warna status bisa diturunkan dari satu state.
 * Enum aman disimpan oleh rememberSaveable (masuk ke Bundle).
 * ============================================================ */
enum class StatusPesanan(val pesan: String) {
    IDLE("Silakan pesan tiket"),
    NAMA_KOSONG("Nama masih kosong"),
    MEMPROSES("Memproses pesanan........."),
    SELESAI("Tiket telah dipesan")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PemesananTiket2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    /* ====================================================
                     * STATE HOISTING
                     * Seluruh state dideklarasikan di PARENT (di sini),
                     * bukan di dalam composable TicketScreen.
                     * Data mengalir ke bawah (parameter),
                     * event mengalir ke atas (lambda callback).
                     * ==================================================== */
                    var hargaTiket by rememberSaveable { mutableStateOf(75000) }   // 1. Harga Tiket
                    var jumlahTiket by rememberSaveable { mutableStateOf(1) }      // 2. Jumlah Tiket
                    var namaPembeli by rememberSaveable { mutableStateOf("") }     // 3. Nama Pembeli

                    // State pendukung untuk side-effect
                    var status by rememberSaveable { mutableStateOf(StatusPesanan.IDLE) }
                    var pesanTrigger by rememberSaveable { mutableStateOf(0) }

                    /* ====================================================
                     * SIDE-EFFECT: LaunchedEffect
                     * key = pesanTrigger -> blok dijalankan ulang setiap
                     * tombol "Pesan Tiket" diklik (nilai key berubah).
                     * ==================================================== */
                    LaunchedEffect(pesanTrigger) {
                        if (pesanTrigger == 0) return@LaunchedEffect // abaikan saat pertama masuk komposisi

                        if (namaPembeli.isBlank()) {
                            status = StatusPesanan.NAMA_KOSONG
                        } else {
                            status = StatusPesanan.MEMPROSES
                            delay(5000)                       // simulasi proses 5 detik
                            status = StatusPesanan.SELESAI
                        }
                    }

                    TicketScreen(
                        modifier = Modifier.padding(innerPadding),
                        hargaTiket = hargaTiket,
                        jumlahTiket = jumlahTiket,
                        namaPembeli = namaPembeli,
                        status = status,
                        onHargaChange = { hargaTiket = it },
                        onJumlahChange = { jumlahTiket = it },
                        onNamaChange = {
                            namaPembeli = it
                            if (status == StatusPesanan.NAMA_KOSONG) status = StatusPesanan.IDLE
                        },
                        onPesanClick = { pesanTrigger++ }
                    )
                }
            }
        }
    }
}

/* ============================================================
 * VIEW (STATELESS)
 * Tidak ada remember/mutableStateOf di sini. Semua nilai
 * diterima lewat parameter, semua perubahan dikirim lewat callback.
 * ============================================================ */
@Composable
fun TicketScreen(
    modifier: Modifier = Modifier,
    hargaTiket: Int,
    jumlahTiket: Int,
    namaPembeli: String,
    status: StatusPesanan,
    onHargaChange: (Int) -> Unit,
    onJumlahChange: (Int) -> Unit,
    onNamaChange: (String) -> Unit,
    onPesanClick: () -> Unit
) {
    val sedangMemproses = status == StatusPesanan.MEMPROSES
    val total = hargaTiket * jumlahTiket

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Pemesanan Tiket",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.size(24.dp))

        // ---------- Nama Pembeli ----------
        Text("Nama Pembeli", fontWeight = FontWeight.Medium)
        Spacer(Modifier.size(8.dp))
        OutlinedTextField(
            value = namaPembeli,
            onValueChange = onNamaChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !sedangMemproses,
            isError = status == StatusPesanan.NAMA_KOSONG,
            placeholder = { Text("Masukkan nama Anda") }
        )
        Spacer(Modifier.size(20.dp))

        // ---------- Harga Tiket ----------
        Text("Harga Tiket", fontWeight = FontWeight.Medium)
        Spacer(Modifier.size(8.dp))
        OutlinedTextField(
            value = hargaTiket.toString(),
            onValueChange = { onHargaChange(it.toIntOrNull() ?: 0) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !sedangMemproses,
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.size(20.dp))

        // ---------- Jumlah Tiket ----------
        Text("Jumlah Tiket", fontWeight = FontWeight.Medium)
        Spacer(Modifier.size(8.dp))
        JumlahSelector(
            jumlah = jumlahTiket,
            enabled = !sedangMemproses,
            onJumlahChange = onJumlahChange
        )
        Spacer(Modifier.size(20.dp))

        HorizontalDivider()
        Spacer(Modifier.size(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Bayar", fontSize = 18.sp)
            Text(
                text = "Rp ${"%,d".format(total)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.size(24.dp))

        Button(
            onClick = onPesanClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !sedangMemproses
        ) {
            Text(if (sedangMemproses) "Memproses..." else "Pesan Tiket")
        }
        Spacer(Modifier.size(20.dp))

        StatusBar(status = status)
    }
}

/* ---------- Komponen stateless: pemilih jumlah tiket ---------- */
@Composable
fun JumlahSelector(
    jumlah: Int,
    enabled: Boolean,
    onJumlahChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { if (jumlah > 1) onJumlahChange(jumlah - 1) },
            enabled = enabled && jumlah > 1
        ) { Text("-") }

        Text(
            text = "$jumlah",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        OutlinedButton(
            onClick = { onJumlahChange(jumlah + 1) },
            enabled = enabled
        ) { Text("+") }
    }
}

/* ---------- Komponen stateless: baris status ---------- */
@Composable
fun StatusBar(status: StatusPesanan) {
    val warnaLatar = when (status) {
        StatusPesanan.IDLE -> Color(0xFFF1F3F5)
        StatusPesanan.NAMA_KOSONG -> Color(0xFFFDECEC)
        StatusPesanan.MEMPROSES -> Color(0xFFE8F1FD)
        StatusPesanan.SELESAI -> Color(0xFFE7F6EC)
    }
    val warnaTeks = when (status) {
        StatusPesanan.IDLE -> Color(0xFF444444)
        StatusPesanan.NAMA_KOSONG -> Color(0xFFC62828)
        StatusPesanan.MEMPROSES -> Color(0xFF1565C0)
        StatusPesanan.SELESAI -> Color(0xFF2E7D32)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(warnaLatar, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            StatusPesanan.MEMPROSES -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = warnaTeks
            )
            StatusPesanan.SELESAI -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = warnaTeks,
                modifier = Modifier.size(18.dp)
            )
            else -> Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = warnaTeks,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.size(10.dp))
        Text(
            text = "Status: ${status.pesan}",
            color = warnaTeks,
            fontWeight = FontWeight.Medium
        )
    }
}

/* ---------- Preview (tetap stateless) ---------- */
@Preview(showBackground = true)
@Composable
fun TicketScreenPreview() {
    PemesananTiket2Theme {
        TicketScreen(
            hargaTiket = 75000,
            jumlahTiket = 2,
            namaPembeli = "Budi Santoso",
            status = StatusPesanan.SELESAI,
            onHargaChange = {},
            onJumlahChange = {},
            onNamaChange = {},
            onPesanClick = {}
        )
    }
}
