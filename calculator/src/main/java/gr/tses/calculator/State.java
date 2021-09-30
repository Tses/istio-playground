package gr.tses.calculator;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.RandomStringUtils;

@ApplicationScoped
public class State {
    private int exception = 0;
    private int timeout = 0;

    public String call() throws InterruptedException {
        System.out.println("Random characters pseudo engine started, emit exception:" + exception + " calculation pseudo effort:" + timeout );
        String s = "";
        if (exception == 1) {
            throw new RuntimeException();
        }
        if (this.timeout != 0) {            
            Thread.sleep(timeout);
            s = RandomStringUtils.randomAlphanumeric(10);
            System.out.println("Random characters pseudo engine  completed, result:" + s);
        } else {
            s = RandomStringUtils.randomAlphanumeric(10);
            System.out.println("Random characters pseudo engine  completed, result:" + s);
        }
        return  s;
    }

    public int getException() {
        return exception;
    }

    public void setException(int exception) {
        this.exception = exception;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
