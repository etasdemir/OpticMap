package com.elacqua.opticmap.util

enum class Languages(val shortName: String) {
    Catalan("ca"),
    Danish("da"),
    Dutch("nl"),
    English("en"),
    Finnish("fi"),
    French("fr"),
    German("de"),
    Hungarian("hu"),
    Italian("it"),
    Norwegian("no"),
    Polish("pl"),
    Portugese("pt"),
    Romanian("ro"),
    Spanish("es"),
    Swedish("sv"),
    Tagalog("tl"),
    Turkish("tr");

    companion object {
        fun availableLanguages(): Array<String> {
            val langList = ArrayList<String>()
            for (lang in values()) {
                langList.add(lang.name)
            }
            return langList.toTypedArray()
        }

        fun getLanguageFromShortName(_shortName: String): Languages {
            var result = Constant.DEFAULT_LANGUAGE
            for (lang in values()) {
                if (lang.shortName == _shortName) {
                    result = lang
                }
            }
            return result
        }
    }
}

enum class Language {
    FROM,TO
}