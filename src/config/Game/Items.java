
package config.Game;

/**
 *
 * @author JavaScriptz
 */
public class Items {

    public static boolean isUse(int itemid) {
        return itemid >= 2000000 && itemid < 3000000;
    }

    public static boolean isEquip(int itemid) {
        return itemid < 2000000;
    }

    public static boolean isEtc(int itemid) {
        return itemid >= 4000000 && itemid < 5000000;
    }

    public static boolean isCash(int itemid) {
        return itemid >= 5000000;
    }

    public static boolean isSetup(int itemid) {
        return itemid >= 3000000 && itemid < 4000000;
    }

    public static boolean isCurrency(int itemid){
        return itemid >= 4032015 && itemid < 4032017;
    }

    public static class Cash {

        public final static int TeleportRock = 5040000;
        public final static int VIPTeleRock = 5041000;
        public final static int APReset = 5050000;
        public final static int FirstJobSPReset = 5050001;
        public final static int SecondJobSPReset = 5050002;
        public final static int ThirdJobSPReset = 5050003;
        public final static int FourthJobSPReset = 5050004;
        public final static int Megaphone = 5071000;
        public final static int SuperMegaphone = 5072000;
        public final static int ItemMegaphone = 5076000;
        public final static int TripleMegaphone = 5077000;
        public final static int Note = 5090000;
        public final static int PetNameTag = 5170000;
        public final static int ViciousHammer = 5570000;
        public final static int DiabloMessenger = 5390000;
        public final static int Cloud9Messenger = 5390001;
        public final static int LoveholicMessenger = 5390002;
        public final static int Chalkboard1 = 5370000;
        public final static int Chalkboard2 = 5370001;

        public static boolean isSPReset(int itemId) {
            switch (itemId) {
                case FirstJobSPReset:
                case SecondJobSPReset:
                case ThirdJobSPReset:
                case FourthJobSPReset:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isAvatarMega(int itemId) {
            switch (itemId) {
                case DiabloMessenger:
                case Cloud9Messenger:
                case LoveholicMessenger:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean isPetFood(int itemId) {
            if (itemId >= 5240000 && itemId <= 5240020) {
                return true;
            }
            return false;
        }
    }

    public static class currencyType {

        public final static int Sight = 4032016;
        public final static int Harmony = 4032017;
        public final static int Shadow = 4032015;
    }

    public enum MegaPhoneType {

        MEGAPHONE(2),
        SUPERMEGAPHONE(3),
        ITEMMEGAPHONE(8);
        private int i;

        MegaPhoneType(int i) {
            this.i = i;
        }

        public int getValue() {
            return i;
        }
    }
    public static final boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static final boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }

    public static final boolean isRechargable(int itemId) {
        return itemId / 10000 == 233 || itemId / 10000 == 207;
    }

    public static final boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static final boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }
}
