package com.zelaux.numberconverter.numbertype;

import com.intellij.lang.Language;
import com.intellij.util.IntPair;
import com.zelaux.numberconverter.NumberContainer;
import com.zelaux.numberconverter.extensionpoints.NumberTypeProvider;
import com.zelaux.numberconverter.extensionpoints.RadixNumberTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class NumberTypeContainer {
    static HashMap<String, ArrayList<NumberType>> types = null;
    static HashMap<String, EnumMap<DefaultRadixNumberType, NumberType.RadixType>> defaultRadixTypes = null;

    static HashMap<String, ArrayList<NumberType>> getTypes() {
        if (types != null) return types;

//        types = NumberTypeProvider.EP_NAME.extensions()
        types = new HashMap<>();
        defaultRadixTypes = new HashMap<>();
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
        EnumMap<DefaultRadixNumberType, NumberType.RadixType> enumMap = new EnumMap<>(DefaultRadixNumberType.class);
        defaultRadixTypes.put(language.getID(), enumMap);
        if (radixNumberTypeProvider != null) {
            addIfNotNull(list, enumMap, DefaultRadixNumberType.Decimal, radixNumberTypeProvider.decimal());
            addIfNotNull(list, enumMap, DefaultRadixNumberType.Binary, radixNumberTypeProvider.binary());
            addIfNotNull(list, enumMap, DefaultRadixNumberType.Octal, radixNumberTypeProvider.octal());
            addIfNotNull(list, enumMap, DefaultRadixNumberType.Hexadecimal, radixNumberTypeProvider.hexadecimal());
        }
        if (shouldAddAny) {

            ArrayList<NumberType> anyTypes = types.get(Language.ANY.getID());
            if (radixNumberTypeProvider == null) {
                list.addAll(anyTypes);
                for (DefaultRadixNumberType value : DefaultRadixNumberType.values()) {
                    enumMap.put(value, value);
                }
            } else {
                for (NumberType type : anyTypes) {
                    if (type instanceof DefaultRadixNumberType) continue;
                    list.add(type);
                }
            }
        }
        return list;
    }

    private static void addIfNotNull(ArrayList<NumberType> list,
                                     EnumMap<DefaultRadixNumberType, NumberType.RadixType> typeEnumMap,
                                     DefaultRadixNumberType type,
                                     @Nullable NumberType.RadixType numberType) {
        if (numberType == null) return;
        list.add(numberType);
        typeEnumMap.put(type, numberType);
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
    String title();

    static Stream<Map.Entry<DefaultRadixNumberType, NumberType.RadixType>> getRadixTypes(Language language) {
        return NumberTypeContainer.defaultRadixTypes.get(language.getID()).entrySet().stream();
    }

    final Pattern numberPattern = Pattern.compile("[1-9][0-9]*");

    default boolean isDecimal() {
        return this == DefaultRadixNumberType.Decimal;
    }

    @Nullable
    public static NumberType of(NumberContainer value, int inElementStart, int inElementEnd) {
        ArrayList<NumberType> all = NumberTypeContainer.getTypes(value.language);

//        if (value.equals("0") || numberPattern.matcher(value).matches()) return DefaultRadixNumberType.decimal;
//        value = value.toLowerCase();
        return of(value, inElementStart, inElementEnd, all.stream());
    }

    @Nullable
    public static NumberType of(NumberContainer value, int inElementStart, int inElementEnd, Stream<NumberType> numberTypeStream) {
        return numberTypeStream
                .filter(it -> it.match(value, inElementStart, inElementEnd))
                .findFirst().orElse(null);
    }

    public static BigInteger parseStatic(NumberContainer value, int inElementStart, int inElementEnd) {
        NumberType numberType = of(value, inElementStart, inElementEnd);
        if (numberType == null) throw new NullPointerException();
        return numberType.parse(value, inElementStart, inElementEnd);
    }

    boolean match(NumberContainer container, int inElementStart, int inElementEnd);

    BigInteger parse(NumberContainer container, int inElementStart, int inElementEnd);

    PsiResult wrap(NumberContainer container, BigInteger integer);

    interface RadixType extends NumberType {
        /**
         * @return null if underscores is unsupported
         */
        @Nullable
        IntPair numberContentRange(NumberContainer container, int inElementStart, int inElementEnd);

        String wrapUnderScore(String numberWithUnderScore);

        @NotNull
        static IntPair clearWhiteSpaces(String text, int beginOffset, int endOffset) {
            while (text.charAt(beginOffset) == ' ') {
                beginOffset++;
            }
            while (text.charAt(text.length() - 1 - endOffset) == ' ') {
                endOffset++;
            }
            return new IntPair(beginOffset, text.length() - endOffset);
        }
    }

    interface MatchByPattern extends NumberType {
        Pattern pattern();

        @Override
        default boolean match(NumberContainer container, int inElementStart, int inElementEnd) {
            return pattern().matcher(container.getText(inElementStart, inElementEnd)).matches();
        }
    }

}