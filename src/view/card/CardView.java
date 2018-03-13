package view.card;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardView{

    public CardView() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("../../images/card-backwards.jpg"));
        } catch (IOException e) {

        }
    }


}
