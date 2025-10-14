package _dev;

import dev.jeka.core.tool.JkJekaVersionRanges;
import dev.jeka.core.tool.JkPostInit;
import dev.jeka.core.tool.KBean;
import dev.jeka.core.tool.builtins.base.BaseKBean;

class Custom extends KBean {

    @JkPostInit
    private void postInit(BaseKBean baseKBean) {
        JkJekaVersionRanges.setCompatibilityRange(baseKBean.getManifest(),
                "0.11.55",
                "https://raw.githubusercontent.com/jeka-dev/javafx-plugin/master/breaking_versions.txt");
    }

}