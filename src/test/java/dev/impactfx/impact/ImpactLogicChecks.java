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
        check(ImpactPreset.MINIMAL.next()==ImpactPreset.OVERDRIVE,"Overdrive is selectable");
        check(ImpactSequence.beat(0f)==ImpactSequence.Beat.SNAP,"snap start");
        check(ImpactSequence.beat(.12f)==ImpactSequence.Beat.STRIKE,"strike beat");
        check(ImpactSequence.beat(.40f)==ImpactSequence.Beat.SHOCK,"shock beat");
        check(ImpactSequence.beat(.95f)==ImpactSequence.Beat.ECHO,"echo beat");
        check(ImpactSequence.power(.2f,.8f,100)>=ImpactSequence.power(.2f,.8f,0),"drama scales");
        check(ImpactSequence.ringProgress(.2f)==0f && ImpactSequence.ringProgress(.9f)==1f,"ring clamps");
        ImpactConfig c=new ImpactConfig();
        Path temporary=Files.createTempFile("impactfx-check-", ".properties");
        c.durationMs=240; c.durationScale=400; c.macePreset=ImpactPreset.INK; c.saveAt(temporary);
        ImpactConfig loaded=ImpactConfig.loadAt(temporary);
        check(loaded.durationMs==240 && loaded.durationScale==400 && loaded.macePreset==ImpactPreset.INK,"round trip config");
        Files.deleteIfExists(temporary);
        ImpactEngine engine=new ImpactEngine();
        long start=2_000_000_000L;
        check(engine.trigger(c,ImpactEngine.Kind.EXPLOSION,0,0,0,.85f,start,false),"first event");
        check(!engine.trigger(c,ImpactEngine.Kind.EXPLOSION,0,0,0,.85f,start+30_000_000L,false),"dedup");
        check(engine.frame(start+250_000_000L)!=null,"extended frame remains active");
        check(engine.frame(start+960_000_000L)==null,"extended frame eventually expires");
        check(engine.trigger(c,ImpactEngine.Kind.MACE,1,1,1,.8f,start+961_000_000L,false),"new event");
        check(engine.frame(start+965_000_000L).preset()==ImpactPreset.INK,"mace preset");
        check(ImpactMath.eventStrength(6,100,40)==0,"distant blasts are ignored");
        ImpactConfig high=new ImpactConfig();
        high.macePreset=ImpactPreset.OVERDRIVE;
        ImpactEngine over=new ImpactEngine();
        check(over.trigger(high,ImpactEngine.Kind.MACE,0,0,0,1f,start,false),"overdrive triggers");
        ImpactEngine.Frame heavy=over.frame(start+10_000_000L);
        check(heavy.preset()==ImpactPreset.OVERDRIVE,"overdrive selected");
        check(heavy.durationNanos()==(high.durationMs * high.durationScale/100+120)*1_000_000L,"overdrive extended duration");
        check(ImpactSequence.power(.58f,.8f,100) > .7f,"long impact holds intensity");
        Path second=Files.createTempFile("impactfx-config-power", ".properties");
        high.drama=40; high.shockwave=false; high.screenPunch=false;
        high.saveAt(second);
        ImpactConfig checkCfg=ImpactConfig.loadAt(second);
        check(checkCfg.drama==40 && !checkCfg.shockwave && !checkCfg.screenPunch,"power settings round-trip");
        Files.deleteIfExists(second);
        System.out.println("PASS: ImpactFX logic checks");
    }
}
