package org.orbisgis.core.renderer.se.parameter.real;

import java.util.Iterator;
import javax.xml.bind.JAXBElement;
import net.opengis.se._2_0.core.CategorizeType;
import net.opengis.se._2_0.core.ParameterValueType;
import net.opengis.se._2_0.core.ThresholdBelongsToType;
import org.gdms.data.SpatialDataSourceDecorator;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;
import org.orbisgis.core.renderer.se.parameter.Categorize;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;

public final class Categorize2Real extends Categorize<RealParameter, RealLiteral> implements RealParameter {

        private RealParameterContext ctx;

        public Categorize2Real(RealParameter initialClass, RealLiteral fallback, RealParameter lookupValue) {
                super(initialClass, fallback, lookupValue);
                this.setContext(ctx);
        }

        public Categorize2Real(JAXBElement<CategorizeType> expr) throws InvalidStyle {
                CategorizeType t = expr.getValue();


                this.setFallbackValue(new RealLiteral(t.getFallbackValue()));
                this.setLookupValue(SeParameterFactory.createRealParameter(t.getLookupValue()));


                Iterator<JAXBElement<ParameterValueType>> it = t.getThresholdAndValue().iterator();

                this.setClassValue(0, SeParameterFactory.createRealParameter(it.next().getValue()));

                // Fetch class values and thresholds
                while (it.hasNext()) {
                        this.addClass(SeParameterFactory.createRealParameter(it.next().getValue()),
                                SeParameterFactory.createRealParameter(it.next().getValue()));
                }

        if (t.getThresholdBelongsTo() == ThresholdBelongsToType.PRECEDING)
            this.setThresholdsPreceding();
        else
            this.setThresholdsSucceeding();
             
        super.setPropertyFromJaxB(t);
    }

    @Override
    public Double getValue(SpatialDataSourceDecorator sds, long fid) throws ParameterException{

		if (sds == null){
			throw new ParameterException("No feature");
		}

		return getParameter(sds, fid).getValue(sds, fid);
    }


	@Override
	public void setClassValue(int i, RealParameter value){
		super.setClassValue(i, value);
		if (value != null){
			value.setContext(ctx);
		}
	}

	@Override
	public void setFallbackValue(RealLiteral l){
		super.setFallbackValue(l);
		if (l != null){
			l.setContext(ctx);
		}
	}

	@Override
	public void setContext(RealParameterContext ctx) {
		this.ctx = ctx;
		this.getFallbackValue().setContext(ctx);

		for (int i=0; i<this.getNumClasses();i++){
			RealParameter classValue = this.getClassValue(i);
			classValue.setContext(ctx);
		}

	}

	@Override
	public String toString(){
		return "NA";
	}

	@Override
	public RealParameterContext getContext() {
		return ctx;
	}

    @Override
    public int compareTo(Object o) {
        return 0;
    }

}
