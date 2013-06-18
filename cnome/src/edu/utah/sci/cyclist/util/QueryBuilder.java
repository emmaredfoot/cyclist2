
package edu.utah.sci.cyclist.util;

import java.util.ArrayList;
import java.util.List;

import edu.utah.sci.cyclist.model.Field;
import edu.utah.sci.cyclist.model.FieldProperties;
import edu.utah.sci.cyclist.model.Filter;
import edu.utah.sci.cyclist.model.Table;

public class QueryBuilder {
	
	private Table _table;
	private List<Field> _fields = new ArrayList<>();
	private List<Filter> _filters = new ArrayList<>();
	private List<Field> _aggregates = new ArrayList<>();
	private List<Field> _grouping = new ArrayList<>();
	private int _limit = -1;
	
	public QueryBuilder(Table table) {
		_table = table;
	}
	
	public QueryBuilder fields(List<Field> list) {
		for (Field f : list)
			field(f);
		return this;
	}
	
	public QueryBuilder limit(int n) {
		_limit = n;
		return this;
	}
	
	public QueryBuilder field(Field field) {
		if (!_fields.contains(field)) {
//			if (field.getRole() == Role.MEASURE && field.getString(FieldProperties.AGGREGATION_FUNC) != null) {
//				_aggregates.add(field);
//			} else {
				_fields.add(field);
//			}
		}
		return this;
	}
	
	public QueryBuilder aggregates(List<Field> list) {
		_aggregates = list;
		return this;
	}
	
	public QueryBuilder grouping(List<Field> list) {
		_grouping = list;
		return this;
	}
	
	private boolean append(StringBuilder builder, boolean first, List<Field> list) {
		for (Field field : list) {
			if (first) {
				builder.append(" ");
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(field.getName());
		}
		
		return first;
	}
	
	public List<Field> getOrder() {
		List<Field> order = new ArrayList<>();
		order.addAll(_fields);
		order.addAll(_aggregates);
		order.addAll(_grouping);
		
		return order;
	}
	
	public String toString() {
		boolean first = true;
		StringBuilder builder = new StringBuilder("Select ");
		
		// dims
		first = append(builder, first, _fields);
			
		// aggregates
		for (Field field : _aggregates) {
			if (first) {
				builder.append(" ");
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(SQL.getFunction(field.getString(FieldProperties.AGGREGATION_FUNC)).format(field.getName()));
		}
		
		append(builder, first, _grouping);
		
		// table
		builder.append(" from ").append(_table.getName());
		
		// group by
		
		first = true;
		if (_aggregates.size() > 0 &&  (_fields.size() > 0 || _grouping.size() > 0)) {
			builder.append(" group by");
			first = append(builder, first, _fields);
			append(builder, first, _grouping);
		}
		
		// filters
		first = true;
		if (_filters.size() > 0) {
			builder.append(" having");
			for (Filter filter : _filters) {
				if (first) {
					builder.append(" ");
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(filter.toString());
			}
		}
		
		// order by
		// TODO
		
		// limit
		if (_limit > 0) {
			builder.append(" limit ").append(_limit);
		}
		
		return builder.toString();
	}
	
}