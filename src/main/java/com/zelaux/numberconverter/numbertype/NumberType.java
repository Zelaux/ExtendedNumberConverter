package com.zelaux.numberconverter.numbertype;

import com.intellij.lang.Language;
import com.intellij.openapi.graph.view.RadialBackgroundRenderer;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import static kotlin.reflect.jvm.internal.impl.utils.CollectionsKt.addIfNotNull;

class NumberTypeContainer {
    static HashMap<String, ArrayList<NumberType>> types = null;

    static HashMap<String, ArrayList<NumberType>> getTypes() {
        if (types != null) return types;

//        types = NumberTypeProvider.EP_NAME.extensions()
        types = new HashMap<>();
        ArrayList<NumberType> anyLang = addLang(Language.ANY);
        anyLang.addAll(Arrays.asList(DefaultRadixNumberType.values()));

        for (Language language : Language.getRegisteredLanguages()) {
            if (language == Language.ANY) continue;
            ArrayList<NumberType> list = addLang(language);
        }
        ArrayList<NumberType> providers = types.putIfAbsent(Language.ANY.getID(), new ArrayList<>());
        assert providers != null;
        providers.addAll(Arrays.asList(DefaultRadixNumberType.values()));
        return types;
    }

    @NotNull
    private static ArrayList<NumberType> addLang(Language language) {
        List<NumberTypeProvider> providers = NumberTypeProvider.LANG_EP.allForLanguage(language);
        if (!types.containsKey(language.getID())) {
            types.put(language.getID(), new ArrayList<>());
        }
        ArrayList<NumberType> list = types.get(language.getID());

        assert list != null;
        boolean shouldAddAny = list.size() == 0 && language != Language.ANY;
        for (NumberTypeProvider provider : providers) {
            list.addAll(Arrays.asList(provider.getNumberTypes()));
        }
        RadixNumberTypeProvider radixNumberTypeProvider = RadixNumberTypeProvider.LANG_EP.forLanguage(language);
        if (radixNumberTypeProvider != null) {
            addIfNotNull(list, radixNumberTypeProvider.decimal());
            addIfNotNull(list, radixNumberTypeProvider.binary());
            addIfNotNull(list, radixNumberTypeProvider.octal());
            addIfNotNull(list, radixNumberTypeProvider.hexadecimal());
        }
        if (shouldAddAny) {

            ArrayList<NumberType> anyTypes = types.get(Language.ANY.getID());
            if (radixNumberTypeProvider == null) {
                list.addAll(anyTypes);
            } else {
                for (NumberType type : anyTypes) {
                    if (type instanceof DefaultRadixNumberType) continue;
                    list.add(type);
                }
            }
        }
        return list;
    }

    public static ArrayList<NumberType> getTypes(@NotNull Language language) {
        ArrayList<NumberType> list = getTypes().get(language.getID());
        if (list == null || list.isEmpty()) {
//            return addLang(language);
        }
        return list;
    }
}

public interface NumberType {

    final Pattern numberPattern = Pattern.compile("[1-9][0-9]*");

    default boolean isDecimal() {
        return this == DefaultRadixNumberType.decimal;
    }

    @Nullable
    public static NumberType of(String value, @Nullable Language language) {
        if (language == null) language = Language.ANY;
        ArrayList<NumberType> all = NumberTypeContainer.getTypes(language);
        value = value.trim();
        if (value.startsWith("-"))
            value = value.substring(1);

        if (value.equals("0") || numberPattern.matcher(value).matches()) return DefaultRadixNumberType.decimal;
//        value = value.toLowerCase();
        int size = all.size();
        for (int i = 0; i < size; i++) {
            NumberType system = all.get(i);
            if (system.match(value, language)) {
                return system;
            }
        }
        return null;
    }

    boolean match(String value, Language language);

    BigInteger parse(String value, Language language);

    String wrap(BigInteger integer, Language language);
}