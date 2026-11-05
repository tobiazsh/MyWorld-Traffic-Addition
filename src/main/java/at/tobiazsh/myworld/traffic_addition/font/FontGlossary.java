package at.tobiazsh.myworld.traffic_addition.font;

import java.util.HashSet;
import java.util.Set;

/**
 * Font glossary that contains all known fonts.
 * Unknown fonts shall not be used, as not everyone connected to the server may have them.
 */
public class FontGlossary {

    /**
     * Set of known default fonts that are shipped with the mod. This does not mean all fonts are marked as default
     * when using them, but rather that they are known by default.
     */
    private static final Set<LogicalFont> DEFAULT_FONTS = Set.of(
            LogicalFont.of("7da195a74c55bef988d0d48f9508bd5d849425c1770dba5d7bfc6ce9ed848954"), // DejaVu Sans
            LogicalFont.of("e6476c1b80502924294eed40894c5b18e06c181444ca953e5334262df9c27724"), // DejaVu Sans Bold
            LogicalFont.of("3cdbdd35c7637ff4a15d22bdcb8b952f48afadd2e41edb5ab29d80eaad8d48fd"), // DIN 1451 Mittelschrift Regular
            LogicalFont.of("fa90d0ce9386ad0d856f5af26eb16a3a78ad7850edee3d702f54984e8e34f741"), // Funnelsans Bold Italic
            LogicalFont.of("16d205d96b643f282cd269d56045b28beda484c004c5cbf9ede2fe3cab5e5a8a"), // Funnelsans Bold
            LogicalFont.of("c7a91200c3c18b56b89bc1a8717487c8284bddf247d6b97e64f27018ce500d64"), // Funnelsans Italic
            LogicalFont.of("4d3f30a44ebc35f481e58d900d047841fee7a23fd3b8254440ddb22d9d918278"), // Funnelsans Regular
            LogicalFont.of("f6a283cb853e4ef126a4aa343b25132dc89483de15837a9bb004a098bf28e6c2"), // Highway Gothic Condensed
            LogicalFont.of("fc5d1101acaf2512e77c9247a86f63a459f49d7c0f7c795134f53c569ecc834c"), // Highway Gothic Regular
            LogicalFont.of("1332b94f90e10c6acb72a4240b9ae456a70b5bf1d181117cb7512b2e84927d0f"), // Highway Gothic Wide
            LogicalFont.of("ba98ccdbc8ef645a4aab3d758ec4e418ac13664526804ba7f2ad211a266ca7a1"), // Paneuropa Highway
            LogicalFont.of("af6ec537c089d2543c62caac4d86a85cd4b2576ea1ee4f49d8fc28d240e4bb3c"), // Paneuropa Regular
            LogicalFont.of("401e729dff1e984d21fa71377e03ac972b8e65ad232c7fc472150eac94dc3ef8"), // Paneuropa Street
            LogicalFont.of("4b453a2dea3efe2b751251e33698fb3c9861b7d6669c90eef632ec8fd133d9ed"), // Roboto Bold Italic
            LogicalFont.of("8e8cb127554bdd9c8685788dce557e2725a9b62e183d9151fb506b3007ca6a07"), // Roboto Bold
            LogicalFont.of("80fa6b213337799be5c7184fa9cfd37f7cf72c3313cc9ddc4c41124c00af9c19"), // Roboto Italic
            LogicalFont.of("c06fe92ae6e3eae20c1d40a312160ba8c1fc6c1fc39a136a837836e40563ce33"), // Roboto Mono Bold Italic
            LogicalFont.of("3f4e0cd7747df32a34748a66c1274d76712cab429c75e75f5f0167319b97fccb"), // Roboto Mono Bold
            LogicalFont.of("d357b7530d41b1562745a9fad9239565b83e1f1d81164a27c225e936450ad39f"), // Roboto Mono Italic
            LogicalFont.of("8d9a1fa4dfe5a5deea780f6513996141425ef782d7cc5665d4d459146fd3a71c"), // Roboto Mono Regular
            LogicalFont.of("92a62766331140ca5bc8bba52028422d276061ad5717c1ac508a118b619c6aec")  // Roboto Regular
    );

    private static final FontGlossary INSTANCE = new FontGlossary();

    private final Set<LogicalFont> knownFonts = new HashSet<>(DEFAULT_FONTS);

    public static FontGlossary getInstance() {
        return INSTANCE;
    }

    public Set<LogicalFont> getKnownFonts() {
        return Set.copyOf(knownFonts);
    }

    public Set<LogicalFont> getDefaultFonts() {
        return DEFAULT_FONTS;
    }

    public void meetFont(LogicalFont font) {
        knownFonts.add(font);
    }
}
