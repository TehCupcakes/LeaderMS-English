/*
 * TehCupcakes (tehcupcakes@hotmail.com)
 * LeaderMS English 2015
 * Maplestory Private Server
 */
package config.configuration;

/**
 * 
 * @author TehCupcakes
 */
public class FeatureManager {
    //All toggleable features should be listed in this enum
    public static enum FeatureName {
        JobAdvancer //Turns Cody into an all-in-one job advancer
    }
    
    
    
    public final Feature[] featureList = {
        //This is "create" features and mark them on ar off.
        //You can also set a "priority" for what features should override others.
        new Feature(FeatureName.JobAdvancer, false, 1)
    };
    
    private Feature findFeature(FeatureName f) {
        for(int i = 0; i < this.featureList.length; i++) {
            if(this.featureList[i].getId().equals(f))
                return this.featureList[i];
        }
        return null;
    }
    
    public boolean isEnabled(FeatureName f) {
        return (findFeature(f) != null) ? findFeature(f).isEnabled() : false;
    }
    
    public int count() {
        return featureList.length;
    }
    
    public FeatureManager() { }
    
    
    
    public class Feature {
        private FeatureName _id;
        private boolean _enabled;
        private int _priority;
        
        protected Feature(FeatureName f) {
            this(f, true, 1);
        }
        
        protected Feature(FeatureName f, boolean enabled) {
            this(f, enabled, 1);
        }
        
        /**
         * @param f         An id for the feature; from enum FeatureName
         * @param enabled   Whether or not to not to enable to the feature
         * @param priority  Higher priority scripts override lower priority (NOTE: Non-feature scripts have priority of 0)
         */
        protected Feature(FeatureName f, boolean enabled, int priority) {
            _id = f;
            _enabled = enabled;
            _priority = priority;
        }
        
        public FeatureName getId() {
            return _id;
        }
        
        public boolean isEnabled() {
            return _enabled;
        }
        
        public int getPriority() {
            return _priority;
        }
        
        public String getName() {
            return this.toString();
        }
        
        @Override
        public String toString() {
            return Feature.this._id.name();
        }
    }
}