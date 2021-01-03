package com.elacqua.opticmap.util

object Constant {
    const val IMAGE_PICK_INTENT_CODE = 10
    const val CAMERA_REQUEST_CODE = 11
    val languages = arrayOf("Afrikaans", "Amharic", "Arabic", "Assamese", "Azerbaijani",
        "Azerbaijani-Cyrillic", "Belarusian", "Bengali", "Tibetan", "Bosnian", "Bulgarian",
        "Catalan;Valencian", "Cebuano", "Czech", "Chinese-Simplified", "Chinese-Traditional",
        "Cherokee", "Welsh", "Danish", "German", "Dzongkha", "Greek,Modern(1453-)", "English",
        "English,Middle(1100-1500)", "Esperanto", "Estonian", "Basque", "Persian", "Finnish",
        "French", "GermanFraktur", "French,Middle(ca.1400-1600)", "Irish", "Galician",
        "Greek,Ancient(-1453)", "Gujarati", "Haitian;HaitianCreole", "Hebrew", "Hindi",
        "Croatian", "Hungarian", "Inuktitut", "Indonesian", "Icelandic", "Italian",
        "Italian-Old", "Javanese", "Japanese", "Kannada", "Georgian", "Georgian-Old",
        "Kazakh", "CentralKhmer", "Kirghiz;Kyrgyz", "Korean", "Kurdish", "Lao", "Latin",
        "Latvian", "Lithuanian", "Malayalam", "Marathi", "Macedonian", "Maltese", "Malay",
        "Burmese", "Nepali", "Dutch;Flemish", "Norwegian", "Oriya", "Panjabi;Punjabi", "Polish",
        "Portuguese", "Pushto;Pashto", "Romanian;Moldavian;Moldovan", "Russian", "Sanskrit",
        "Sinhala;Sinhalese", "Slovak", "Slovenian", "Spanish;Castilian", "Spanish;Castilian-Old",
        "Albanian", "Serbian", "Serbian-Latin", "Swahili", "Swedish", "Syriac", "Tamil", "Telugu",
        "Tajik", "Tagalog", "Thai", "Tigrinya", "Turkish", "Uighur;Uyghur", "Ukrainian", "Urdu",
        "Uzbek", "Uzbek-Cyrillic", "Vietnamese", "Yiddish")
    val shortLang = arrayOf("afr", "amh", "ara", "asm", "aze", "aze_cyrl", "bel", "ben", "bod",
        "bos", "bul", "cat", "ceb", "ces", "chi_sim", "chi_tra", "chr", "cym", "dan", "deu",
        "dzo", "ell", "eng", "enm", "epo", "est", "eus", "fas", "fin", "fra", "frk", "frm", "gle",
        "glg", "grc", "guj", "hat", "heb", "hin", "hrv", "hun", "iku", "ind", "isl", "ita", "ita_old",
        "jav", "jpn", "kan", "kat", "kat_old", "kaz", "khm", "kir", "kor", "kur", "lao", "lat", "lav",
        "lit", "mal", "mar", "mkd", "mlt", "msa", "mya", "nep", "nld", "nor", "ori", "pan", "pol", "por",
        "pus", "ron", "rus", "san", "sin", "slk", "slv", "spa", "spa_old", "sqi", "srp", "srp_latn",
        "swa", "swe", "syr", "tam", "tel", "tgk", "tgl", "tha", "tir", "tur", "uig", "ukr", "urd",
        "uzb", "uzb_cyrl", "vie", "yid")
}

enum class Language {
    FROM,TO
}
