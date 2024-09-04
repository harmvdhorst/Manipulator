package nl.harmvdhorst.manipulator;

public class MethodData {

    public final Object thisObject;
    public final String method;
    public final Object[] args;

    public MethodData(Object thisObject, String method, Object[] args){
        this.thisObject = thisObject;
        this.method = method;
        this.args = args;
    }

}
