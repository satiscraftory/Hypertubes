package net.ugi.hypertubes.hypertube.Curves;

public class CurveTypes {

    public enum Curves {
        CURVED("curved"),
        OVERKILL("overkill"),
        STRAIGHT("straight"),
        MINECRAFT("minecraft");
        //HELIX("helix");

        private final String name;

        Curves(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public static Curves get(String name) {
            for (Curves curve : Curves.values()) {
                if (curve.name.equals(name)){
                    return curve;
                }
            }
            return null;
        }
    }

    public static Curves cycle(Curves activeCurve) {
        Curves[] all = Curves.values();
        int nextIndex = (activeCurve.ordinal() + 1) % all.length;
        return all[nextIndex];
    }





}