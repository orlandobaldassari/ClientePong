package clientegamepong;

import java.awt.Graphics;
import javaPlay.GameObject;
import javaPlay.Sprite;

/**
 * Classe de definição da tela de fundo do game
 * @author marcondes
 */
class Background extends GameObject{
    Sprite sprite;
    
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
    
    @Override
    public void step(long time) {
        
    }

    @Override
    public void draw(Graphics graphic) {
        sprite.draw(graphic, x, y);
    }
    
}
