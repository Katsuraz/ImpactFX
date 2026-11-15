package dev.impactfx.impact;

import dev.impactfx.config.ImpactConfig;
import dev.impactfx.config.ImpactPreset;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImpactLogicChecks {
    private static void check(boolean condition,String why) { if(!condition)throw new AssertionError(why); }
    public static void main(String[] args) throws Exception {
        check(ImpactMath.eventStrength(4,0,40)>ImpactMath.eventStrength(4,20,40),"distance must attenuate");
        check(ImpactMath.eventStrength(4,40,40)==0,"out-of-range should not trigger");
        check(ImpactMath.alpha(20)==255 && ImpactMath.alpha(-1)==0,"alpha clamp");
        check(ImpactMath.envelope(.25f)>0 && ImpactMath.envelope(1f)==0,"animation envelope");
        check(ImpactPreset.MANGA.next()==ImpactPreset.ANIME,"presets order");
        ImpactConfig c=new ImpactConfig();
        Path temporary=Files.createTempFile("impactfx-check-", ".properties");
        c.durationMs=240; c.macePreset=ImpactPreset.INK; c.saveAt(temporary);
        ImpactConfig loaded=ImpactConfig.loadAt(temporary);
        check(loaded.durationMs==240 && loaded.macePreset==ImpactPreset.INK,"round trip config");
        Files.deleteIfExists(temporary);
        ImpactEngine engine=new ImpactEngine();
        long start=2_000_000_000L;
        check(engine.trigger(c,ImpactEngine.Kind.EXPLOSION,0,0,0,.85f,start,false),"first event");
        check(!engine.trigger(c,ImpactEngine.Kind.EXPLOSION,0,0,0,.85f,start+30_000_000L,false),"dedup");
        check(engine.frame(start+250_000_000L)==null,"expire frame");
        check(engine.trigger(c,ImpactEngine.Kind.MACE,1,1,1,.8f,start+251_000_000L,false),"new event");
        check(engine.frame(start+255_000_000L).preset()==ImpactPreset.INK,"mace preset");
        check(ImpactMath.eventStrength(6,100,40)==0,"distant blasts are ignored");
        System.out.println("PASS: ImpactFX logic checks");
    }
}
