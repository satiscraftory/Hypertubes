package net.ugi.sf_hypertube.hypertube.Curves;

public class CurveTypes {

    public enum Curves {
        CURVED("curved"),
        OVERKILL("overkill"),
        STRAIGHT("straight"),
        MINECRAFT("minecraft");
        //HELIX("helix");

        private final String label;

        Curves(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static Curves get(String label) {
            for (Curves curve : Curves.values()) {
                return curve.label.equals(label) ? curve : null;
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