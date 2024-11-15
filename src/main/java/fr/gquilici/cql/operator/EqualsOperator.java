package fr.gquilici.cql.operator;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import fr.gquilici.cql.Filter;
import fr.gquilici.cql.OperandsParser;
import fr.gquilici.cql.Operator;
import fr.gquilici.cql.PathResolver;
import fr.gquilici.cql.StringExpressionFormatter;
import jakarta.persistence.criteria.Path;

public class EqualsOperator<N> implements Operator<N> {

	private PathResolver pathResolver;
	private OperandsParser<N> operandsParser;
	private StringExpressionFormatter stringExpressionFormatter;

	public EqualsOperator(PathResolver pathResolver, OperandsParser<N> operandsParser) {
		this.pathResolver = pathResolver;
		this.operandsParser = operandsParser;
		this.stringExpressionFormatter = new StringExpressionFormatter();
	}

	@Override
	public <T> Specification<T> build(Filter<N> filter) {
		return (root, query, builder) -> {
			Path<?> path = pathResolver.resolve(filter.property(), root);
			Class<?> type = path.getJavaType();
			// TODO checkAcceptedType(type);

			List<?> operands = operandsParser.parseAs(filter.operands(), type);
			if (operands.get(0) == null) {
				return builder.isNull(path);
			}

			if (type.equals(String.class)) {
				@SuppressWarnings("unchecked")
				var formatPath = stringExpressionFormatter.format(builder, (Path<String>) path, filter.options());
				var formatOperands = stringExpressionFormatter.format(builder, operands, filter.options());
				return builder.equal(formatPath, formatOperands.get(0));
			}
			return builder.equal(path, operands.get(0));
		};
	}

}
