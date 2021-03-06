package Model.Filters.FiltersImpl;

import Model.Filters.AbstractFiltres.SinglePixelFilter;

import java.awt.image.BufferedImage;

final public class NegativeFilter extends SinglePixelFilter {

    @Override
    public void runFiltering(BufferedImage image, int startX, int endX, int startY, int endY) {

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                //get my RGB values
                int[] argb = super.getMyRGB(image, x, y);
                // negative filter
                for(int i=1 ; i<argb.length ; i++)
                    argb[i] = 255 - argb[i];
                //set my new RGB values
                super.setMyRGB(image, x, y,argb);
            }
        }
    }
}
