package dev.impactfx.gui;

import dev.impactfx.ImpactFxClient;
import dev.impactfx.config.ImpactConfig;
import dev.impactfx.config.ImpactPreset;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Four compact configuration pages, without Cloth Config or an extra runtime dependency. */
public final class ImpactConfigScreen extends Screen {
    private final Screen parent;
    private int page = 0;
    private ImpactConfig c;
    private int colWidth;
    private int left;
    private int top;

    public ImpactConfigScreen(Screen parent) {
        super(Component.literal("ImpactFX"));
        this.parent = parent;
        this.c = ImpactFxClient.config;
    }

    @Override
    protected void init() {
        clearWidgets();
        colWidth=Math.min(176,Math.max(118,(width-34)/2));
        left=(width-colWidth*2-10)/2;
        top=Math.max(32,height/2-77);
        button(left,top-25,colWidth/2,20,"< Previous", b -> page=(page+3)%4);
        button(left+colWidth+10+colWidth/2,top-25,colWidth/2,20,"Next >", b -> page=(page+1)%4);
        switch(page) {
            case 0 -> main();
            case 1 -> looks();
            case 2 -> comfort();
            case 3 -> profiles();
        }
        button(left,top+5*24+11,colWidth,20,"Preview explosion",b -> ImpactFxClient.preview(false));
        button(left+colWidth+10,top+5*24+11,colWidth,20,"Preview mace",b -> ImpactFxClient.preview(true));
        button(left,top+5*24+36,colWidth*2+10,20,"Done (save)",b -> onClose());
    }
    private void main() {
        at(0,0,"Mod: "+state(c.enabled), b -> c.enabled=!c.enabled);
        at(1,0,"Explosions: "+state(c.explosions), b -> c.explosions=!c.explosions);
        at(0,1,"Mace: "+state(c.mace), b -> c.mace=!c.mace);
        at(1,1,"Impact intensity: "+c.intensity+"%",b -> c.intensity=cycle(c.intensity,0,100,10));
        at(0,2,"Explosion: "+c.explosionPreset.label,b -> c.explosionPreset=c.explosionPreset.next());
        at(1,2,"Mace: "+c.macePreset.label,b -> c.macePreset=c.macePreset.next());
        at(0,3,"Duration: "+c.durationMs+"ms",b -> c.durationMs=cycle(c.durationMs,60,400,20));
        at(1,3,"Cooldown: "+c.cooldownMs+"ms",b -> c.cooldownMs=cycle(c.cooldownMs,80,700,40));
        at(0,4,"Max distance: "+c.maxDistance+"m",b -> c.maxDistance=cycle(c.maxDistance,8,128,8));
        at(1,4,"Impact Echo: "+state(c.echo),b -> c.echo=!c.echo);
    }
    private void looks() {
        at(0,0,"Speed lines: "+c.speedLines+"%",b -> c.speedLines=cycle(c.speedLines,0,100,10));
        at(1,0,"Palette: "+new String[]{"Classic","Rose","Gold"}[c.palette],b -> c.palette=(c.palette+1)%3);
        at(0,1,"World tint: "+state(c.tintWorld),b -> c.tintWorld=!c.tintWorld);
        at(1,1,"Impact cuts: "+state(c.fullScreenCuts),b -> c.fullScreenCuts=!c.fullScreenCuts);
        at(0,2,"Camera shake: unavailable",null);
        at(1,2,"World shaders: "+state(c.worldShaders),b -> c.worldShaders=!c.worldShaders);
        at(0,3,"Manga / Anime / Ink",null);
        at(1,3,"Retro / Cinematic / Minimal",null);
        at(0,4,"Separate event presets",null);
        at(1,4,"World processing: 1+1 passes",null);
    }
    private void comfort() {
        at(0,0,"Reduced flashes: "+state(c.reducedFlashes),b -> c.reducedFlashes=!c.reducedFlashes);
        at(1,0,"Reduced motion: "+state(c.reducedMotion),b -> c.reducedMotion=!c.reducedMotion);
        at(0,1,"Show with F1: "+state(c.showWithF1),b -> c.showWithF1=!c.showWithF1);
        at(1,1,"Hide in menus: "+state(c.hideDuringGui),b -> c.hideDuringGui=!c.hideDuringGui);
        at(0,2,"Reset to defaults",b -> { c=new ImpactConfig(); ImpactFxClient.config=c; });
        at(1,2,"Profiles on next page",b -> page=3);
        at(0,3,"No gameplay time changes",null);
        at(1,3,"No client tick freeze",null);
        at(0,4,"Shader optional in Visuals",b -> {page=1;});
        at(1,4,"HUD fallback always available",null);
    }
    private void profiles() {
        for (int slot=1;slot<=3;slot++) {
            final int s=slot;
            at(0,slot-1,"Save profile "+slot,b -> c.saveProfile(s));
            at(1,slot-1,"Load profile "+slot,b -> { c=ImpactConfig.loadProfile(s); ImpactFxClient.config=c; });
        }
        at(0,3,"Explosion: "+c.explosionPreset.label,b -> c.explosionPreset=c.explosionPreset.next());
        at(1,3,"Mace: "+c.macePreset.label,b -> c.macePreset=c.macePreset.next());
        at(0,4,"Profiles: config/impactfx",null);
        at(1,4,"Save before testing",null);
    }
    private void at(int column,int row,String label,Consumer<Button> action) {
        button(left+column*(colWidth+10),top+row*24,colWidth,20,label,action);
    }
    private void button(int x,int y,int w,int h,String label,Consumer<Button> action) {
        Button b=Button.builder(Component.literal(label),pressed -> {
            if(action!=null) {
                action.accept(pressed);
                c.save();
                rebuildWidgets();
            }
        }).bounds(x,y,w,h).build();
        if(action==null)b.active=false;
        addRenderableWidget(b);
    }
    private static String state(boolean enabled) { return enabled?"ON":"OFF"; }
    private static int cycle(int value,int min,int max,int step) { return value+step>max?min:value+step; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        super.extractRenderState(g,mouseX,mouseY,delta);
        String[] pages={"Basics","Visuals","Comfort","Saved profiles"};
        g.centeredText(font,Component.literal("ImpactFX / "+pages[page]),width/2,10,0xFFFFFFFF);
        g.centeredText(font,Component.literal("Minecraft 26.3 · Alpha 2 · scene shaders optional"),width/2,22,0xFFA6B8C6);
        // Preview must also render on the config screen, otherwise the HUD is hidden behind it.
        ImpactFxClient.RENDERER.render(g,minecraft,c,ImpactFxClient.ENGINE.frame(System.nanoTime()));
    }

    @Override
    public void onClose() {
        c.save();
        ImpactFxClient.config=c;
        minecraft.gui.setScreen(parent);
    }
}
