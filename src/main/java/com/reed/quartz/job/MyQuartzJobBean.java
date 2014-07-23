package com.reed.quartz.job;

import java.lang.reflect.Method;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 分布式任务调度 
 * quartz.properties 
 * org.quartz.jobStore.isClustered = true 开启集群分布式
 * 
 * @author reed
 * 
 */
public class MyQuartzJobBean extends QuartzJobBean {
	private static Logger logger = LoggerFactory
			.getLogger(MyQuartzJobBean.class);

	private String targetObject;

	private String targetMethod;
	/**
	 * 取自 配置文件中的： <property name="applicationContextSchedulerContextKey"
	 * value="applicationContext" />
	 * applicationContextSchedulerContextKey，默认为applicationContext
	 * ，如改为其他值(bName)，需调用getCtx()
	 */
	private ApplicationContext ctx;

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		try {
			logger.info("execute [" + targetObject + "] at once>>>>>>");
			getCtx(context, "test");
			Object otargetObject = ctx.getBean(targetObject);
			Method m = null;

			try {
				m = otargetObject.getClass().getMethod(targetMethod,
						new Class[] {});
				m.invoke(otargetObject, new Object[] {});
				logger.info("job [" + targetObject + "] success!");
			} catch (SecurityException e) {
				logger.error(e.getMessage());
			} catch (NoSuchMethodException e) {
				logger.error(e.getMessage());
			}
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
	}

	/**
	 * 获取Spring中的上下文
	 * 
	 * @param context
	 * @param bName
	 * @return
	 */
	private void getCtx(JobExecutionContext context, String bName) {
		SchedulerContext schCtx;
		try {
			schCtx = context.getScheduler().getContext();
			//
			this.ctx = (ApplicationContext) schCtx.get(bName);
		} catch (SchedulerException e) {
			logger.error(e.getMessage());
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.ctx = applicationContext;
	}

	public void setTargetObject(String targetObject) {
		this.targetObject = targetObject;
	}

	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

}
