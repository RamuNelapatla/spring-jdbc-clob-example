package classcard.example;

/**
 * Hello world!
 *
 */

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class App
{

    public static void main( String[] args )
    {
		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Module.xml");
		ClobTest obj = (ClobTest) context.getBean("helloBean");
                
                //obj.executeFunctionWithClobParameterAndClobResult();
		obj.callFunctionWithClobParameterAndClobResult();
		
    }
}
