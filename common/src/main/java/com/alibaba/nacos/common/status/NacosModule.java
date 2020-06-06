package com.alibaba.nacos.common.status;

/**
 * @author: jintonghuoya
 * @Date: 2020/06/06 03:20 下午
 * @Description: Nacos模块集合
 */
public enum NacosModule {
    SYSTEM(0, "system", "系统"),
    COMMON(101, "nacos-common", "common模块"),
    ADDRESS(102, "nacos-address", "address模块"),
    API(103, "nacos-api", "api模块"),
    CLIENT(104, "nacos-client", "client模块"),
    CMDB(105, "nacos-cmdb", "cmdb模块"),
    CONFIG(106, "nacos-config", "config模块"),
    CONSISTENCY(107, "nacos-consistency", "consistency模块"),
    CONSOLE(108, "nacos-console", "console模块"),
    CORE(109, "nacos-core", "core模块"),
    EXAMPLE(110, "nacos-example", "example模块"),
    ISTIO(111, "nacos-istio", "istio模块"),
    NAMING(112, "nacos-naming", "naming模块"),
    TEST(113, "nacos-test", "test模块"),
    ;
    private int moduleCode;
    private String moduleName;
    private String moduleDescription;

    NacosModule(int moduleCode, String moduleName, String moduleDescription) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
    }

    public int getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(int moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }
}
