package SuperiorPro.SuperiorPOS.service.util;

import java.awt.image.BufferedImage;

import com.github.anastaciocintra.escpos.image.CoffeeImage;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;

public class CoffeeImageIO {
    public static CoffeeImage read(BufferedImage bufferedImage) {
        return new CoffeeImageImpl(bufferedImage);
    }
}
