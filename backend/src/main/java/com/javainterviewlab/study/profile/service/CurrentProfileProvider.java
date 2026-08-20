package com.javainterviewlab.study.profile.service;

/**
 * 当前学习档案提供者。
 *
 * <p>V0.3 仍是单本地档案，但业务服务不应各自假定“默认档案”如何解析。
 * 未来接入账户上下文时只替换 Provider，不改动答题、进度、复习等业务服务。</p>
 */
public interface CurrentProfileProvider {

    /** 返回当前档案主键；档案缺失时抛出统一受控异常。 */
    Long requireProfileId();
}
