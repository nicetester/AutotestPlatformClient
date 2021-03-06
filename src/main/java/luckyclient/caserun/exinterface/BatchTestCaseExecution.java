package luckyclient.caserun.exinterface;

import luckyclient.caserun.exinterface.TestControl;
import luckyclient.dblog.LogOperation;
import luckyclient.planapi.api.GetServerAPI;
import luckyclient.planapi.entity.TestTaskexcute;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BatchTestCaseExecution {
	
	/**
	 * @param args
	 * @throws ClassNotFoundException
	 * 创建线程池，多线程执行用例
	 */
	
	public static void BatchCaseExecuteForTast(String projectname,String taskid,String batchcase) throws Exception{
		TestTaskexcute task=GetServerAPI.cgetTaskbyid(Integer.valueOf(taskid));
		int threadcount = task.getTestJob().getThreadCount();
		ThreadPoolExecutor	threadExecute	= new ThreadPoolExecutor(threadcount, 30, 3, TimeUnit.SECONDS,
	            new ArrayBlockingQueue<Runnable>(1000),
	            new ThreadPoolExecutor.CallerRunsPolicy());
		if(batchcase.indexOf("ALLFAIL")>-1){    //执行全部非成功状态用例
			LogOperation caselog = new LogOperation();        //初始化写用例结果以及日志模块 
			String casemore = caselog.UnSucCaseUpdate(taskid);
			String temp[]=casemore.split("\\#",-1);
			for(int i=0;i<temp.length;i++){
  			   String testCaseExternalId = temp[i].substring(0, temp[i].indexOf("%"));
			   int version = Integer.parseInt(temp[i].substring(temp[i].indexOf("%")+1,temp[i].length()-1));
			   TestControl.Debugcount++;   //多线程计数++，用于检测线程是否全部执行完
			   threadExecute.execute(new ThreadForBatchCase(projectname,testCaseExternalId,version,taskid));
			}			
		}else{                                           //批量执行用例
			String temp[]=batchcase.split("\\#",-1);
			for(int i=0;i<temp.length;i++){
				String testCaseExternalId = temp[i].substring(0, temp[i].indexOf("%"));
				int version = Integer.parseInt(temp[i].substring(temp[i].indexOf("%")+1,temp[i].length()));
				TestControl.Debugcount++;   //多线程计数++，用于检测线程是否全部执行完
				threadExecute.execute(new ThreadForBatchCase(projectname,testCaseExternalId,version,taskid));
			}
		}
		//多线程计数，用于检测线程是否全部执行完
		int i=0;
		while(TestControl.Debugcount!=0){
			i++;
			if(i>600){
				break;
			}
			Thread.sleep(6000);
		}
		threadExecute.shutdown();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		BatchTestCaseExecution.BatchCaseExecuteForTast("清算项目", "35", "ALLFAIL");
	}

}
