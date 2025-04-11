package org.wallentines.pseudonym.lang;

public interface LocaleHolder {

    String getLanguage();

    static LocaleHolder direct(String language) {
        return new Direct(language);
    }

    record Direct(String language) implements LocaleHolder {
        @Override
        public String getLanguage() {
            return language;
        }
    }

}
