package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CleanerEntity::class,
        RoomEntity::class,
        FeedbackEntity::class,
        ChecklistTaskEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SileniDatabase : RoomDatabase() {
    abstract fun sileniDao(): SileniDao

    companion object {
        @Volatile
        private var INSTANCE: SileniDatabase? = null

        fun getInstance(context: Context): SileniDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): SileniDatabase {
            return Room.databaseBuilder(
                appContext,
                SileniDatabase::class.java,
                "sileni_disperindag_sidoarjo.db",
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(appContext).sileniDao())
                        }
                    }
                })
                .build()
        }

        suspend fun ensureSeeded(dao: SileniDao) {
            if (dao.countCleaners() == 0) {
                seedInitialData(dao)
            }
        }

        private suspend fun seedInitialData(dao: SileniDao) {
            val cleaners = listOf(
                CleanerEntity(
                    id = "PTG-01",
                    name = "Budi Santoso",
                    employeeCode = "PTT-2021-014",
                    assignedZone = "A",
                    zoneTitle = "Zona A • Pimpinan & Ruang Rapat",
                    shiftHours = "06.00 – 14.30 WIB",
                    phoneExt = "Ext. 101",
                    initials = "BS",
                ),
                CleanerEntity(
                    id = "PTG-02",
                    name = "Siti Aminah",
                    employeeCode = "PTT-2022-008",
                    assignedZone = "B",
                    zoneTitle = "Zona B • Pelayanan Publik & Lobby",
                    shiftHours = "06.00 – 14.30 WIB",
                    phoneExt = "Ext. 104",
                    initials = "SA",
                ),
                CleanerEntity(
                    id = "PTG-03",
                    name = "Agus Prasetyo",
                    employeeCode = "PTT-2023-021",
                    assignedZone = "C",
                    zoneTitle = "Zona C • Bidang Teknis & Fasum",
                    shiftHours = "06.30 – 15.00 WIB",
                    phoneExt = "Ext. 109",
                    initials = "AP",
                ),
            )

            val rooms = listOf(
                RoomEntity("R-01", "Ruang Kepala Dinas", "Lantai 2", "A", "Pimpinan", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-01&zona=A"),
                RoomEntity("R-02", "Ruang Rapat Utama", "Lantai 2", "A", "Rapat", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-02&zona=A"),
                RoomEntity("R-03", "Ruang Sekretaris & Kasubbag Umum", "Lantai 2", "A", "Struktural", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-03&zona=A"),
                RoomEntity("R-04", "Area Pelayanan Publik Terpadu", "Lantai 1", "B", "Pelayanan Publik", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-04&zona=B"),
                RoomEntity("R-05", "Lobby Utama & Meja Resepsionis", "Lantai 1", "B", "Area Tamu", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-05&zona=B"),
                RoomEntity("R-06", "Ruang Konsultasi IKM & Metrologi", "Lantai 1", "B", "Pelayanan Publik", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-06&zona=B"),
                RoomEntity("R-07", "Ruang Bidang Perdagangan & Industri", "Lantai 2", "C", "Bidang Teknis", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-07&zona=C"),
                RoomEntity("R-08", "Toilet & Musholla Lantai 1", "Lantai 1", "C", "Fasilitas Umum", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-08&zona=C"),
                RoomEntity("R-09", "Pantry & Toilet Lantai 2", "Lantai 2", "C", "Fasilitas Umum", "https://sileni.disperindag.sidoarjokab.go.id/nilai?room=R-09&zona=C"),
            )

            val now = System.currentTimeMillis()
            val hourMs = 3_600_000L
            val dayMs = 86_400_000L

            val feedbacks = listOf(
                FeedbackEntity(roomId = "R-01", roomName = "Ruang Kepala Dinas", zone = "A", rating = 3, tagsCsv = "Rapi & Harum", comment = "Ruangan sangat bersih, meja rapat tertata rapi sebelum apel pagi.", reporterRole = "Pegawai Internal", timestampMillis = now - 2 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-04", roomName = "Area Pelayanan Publik Terpadu", zone = "B", rating = 3, tagsCsv = "Lantai Bersih", comment = "Area tunggu pemohon nyaman dan lantai mengkilap.", reporterRole = "Tamu Dinas", timestampMillis = now - 4 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-08", roomName = "Toilet & Musholla Lantai 1", zone = "C", rating = 1, tagsCsv = "Bau Kurang Sedap,Lantai Basah", comment = "Toilet tamu lantai 1 tercium bau kurang sedap dan lantai agak licin setelah jam istirahat.", reporterRole = "Tamu Dinas", timestampMillis = now - 5 * hourMs, isResolved = false),
                FeedbackEntity(roomId = "R-02", roomName = "Ruang Rapat Utama", zone = "A", rating = 3, tagsCsv = "Rapi & Harum", comment = "Persiapan ruang rapat koordinasi sangat baik dan bersih.", reporterRole = "Pegawai Internal", timestampMillis = now - 8 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-09", roomName = "Pantry & Toilet Lantai 2", zone = "C", rating = 2, tagsCsv = "Tempat Sampah Penuh", comment = "Tempat sampah di pantry sudah hampir penuh menjelang siang.", reporterRole = "Pegawai Internal", timestampMillis = now - 11 * hourMs, isResolved = false),
                FeedbackEntity(roomId = "R-05", roomName = "Lobby Utama & Meja Resepsionis", zone = "B", rating = 3, tagsCsv = "Lantai Bersih,Rapi & Harum", comment = "Kesan pertama kantor sangat terjaga kebersihannya.", reporterRole = "Tamu Dinas", timestampMillis = now - 1 * dayMs - 3 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-08", roomName = "Toilet & Musholla Lantai 1", zone = "C", rating = 1, tagsCsv = "Tisu/Sabun Habis,Bau Kurang Sedap", comment = "Sabun cuci tangan di wastafel toilet lantai 1 habis.", reporterRole = "Tamu Dinas", timestampMillis = now - 1 * dayMs - 6 * hourMs, isResolved = false),
                FeedbackEntity(roomId = "R-06", roomName = "Ruang Konsultasi IKM & Metrologi", zone = "B", rating = 2, tagsCsv = "Meja Berdebu", comment = "Kaca sekat layanan ada sedikit bercak jari.", reporterRole = "Pegawai Internal", timestampMillis = now - 2 * dayMs - 2 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-03", roomName = "Ruang Sekretaris & Kasubbag Umum", zone = "A", rating = 3, tagsCsv = "Rapi & Harum", comment = "Bersih dan wangi sepanjang hari.", reporterRole = "Pegawai Internal", timestampMillis = now - 2 * dayMs - 5 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-07", roomName = "Ruang Bidang Perdagangan & Industri", zone = "C", rating = 3, tagsCsv = "Rapi & Harum", comment = "Meja kerja staf bersih dan bebas debu.", reporterRole = "Pegawai Internal", timestampMillis = now - 3 * dayMs - 1 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-04", roomName = "Area Pelayanan Publik Terpadu", zone = "B", rating = 3, tagsCsv = "Lantai Bersih", comment = "Pelayanan prima dengan ruang tunggu bersih.", reporterRole = "Tamu Dinas", timestampMillis = now - 3 * dayMs - 7 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-08", roomName = "Toilet & Musholla Lantai 1", zone = "C", rating = 2, tagsCsv = "Lantai Basah", comment = "Area wudhu mohon lebih sering dipel saat jam sholat dzuhur.", reporterRole = "Pegawai Internal", timestampMillis = now - 4 * dayMs - 2 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-01", roomName = "Ruang Kepala Dinas", zone = "A", rating = 3, tagsCsv = "Rapi & Harum", comment = "Sangat memuaskan.", reporterRole = "Pegawai Internal", timestampMillis = now - 4 * dayMs - 6 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-05", roomName = "Lobby Utama & Meja Resepsionis", zone = "B", rating = 3, tagsCsv = "Rapi & Harum", comment = "Lantai dan kaca pintu utama bersih berkilau.", reporterRole = "Tamu Dinas", timestampMillis = now - 5 * dayMs - 3 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-09", roomName = "Pantry & Toilet Lantai 2", zone = "C", rating = 1, tagsCsv = "Tempat Sampah Penuh,Meja Berdebu", comment = "Bekas gelas tamu di pantry belum dibersihkan.", reporterRole = "Pegawai Internal", timestampMillis = now - 5 * dayMs - 8 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-02", roomName = "Ruang Rapat Utama", zone = "A", rating = 2, tagsCsv = "Meja Berdebu", comment = "Sudut ruang rapat perlu vakum tambahan.", reporterRole = "Pegawai Internal", timestampMillis = now - 6 * dayMs - 2 * hourMs, isResolved = true),
                FeedbackEntity(roomId = "R-04", roomName = "Area Pelayanan Publik Terpadu", zone = "B", rating = 3, tagsCsv = "Rapi & Harum", comment = "Nyaman untuk pelaku usaha yang mengurus perizinan.", reporterRole = "Tamu Dinas", timestampMillis = now - 6 * dayMs - 5 * hourMs, isResolved = true),
            )

            val tasks = listOf(
                ChecklistTaskEntity(zone = "A", title = "Sapu, pel & pewangi Ruang Kadis + Ruang Rapat", timeSlot = "06.15 WIB", isCompleted = true),
                ChecklistTaskEntity(zone = "A", title = "Lap meja pimpinan, kaca partisi & cek air minum rapat", timeSlot = "07.15 WIB", isCompleted = true),
                ChecklistTaskEntity(zone = "A", title = "Kosongkan tempat sampah & sanitasi gagang pintu Zona A", timeSlot = "13.00 WIB", isCompleted = false),
                ChecklistTaskEntity(zone = "B", title = "Pel lantai Lobby Utama & Area Pelayanan Publik Lt. 1", timeSlot = "06.15 WIB", isCompleted = true),
                ChecklistTaskEntity(zone = "B", title = "Bersihkan kaca loket layanan & kursi tunggu pemohon", timeSlot = "09.30 WIB", isCompleted = true),
                ChecklistTaskEntity(zone = "B", title = "Cek ulang kebersihan Lobby setelah jam istirahat", timeSlot = "13.15 WIB", isCompleted = false),
                ChecklistTaskEntity(zone = "C", title = "Sikat & disinfeksi Toilet Lt. 1, isi ulang sabun/tisu", timeSlot = "06.30 WIB", isCompleted = true),
                ChecklistTaskEntity(zone = "C", title = "Bersihkan Musholla, sajadah & keringkan area wudhu", timeSlot = "11.15 WIB", isCompleted = false),
                ChecklistTaskEntity(zone = "C", title = "Rapikan Pantry Lt. 2 & angkut sampah Bidang Teknis", timeSlot = "13.30 WIB", isCompleted = false),
            )

            dao.insertCleaners(cleaners)
            dao.insertRooms(rooms)
            dao.insertFeedbacks(feedbacks)
            dao.insertChecklistTasks(tasks)
        }
    }
}
