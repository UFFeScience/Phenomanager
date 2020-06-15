package com.uff.phenomanager.domain.core.filter;

import java.util.Arrays;
import java.util.List;

import com.uff.phenomanager.util.StringParserUtils;

public class RequestFilter {
	
	private String filter;
	private String projection;
	private String sum;
	private String avg;
	private String count;
	private String countDistinct;
	private String groupBy;
	private String sort; 
	private String offset;
	private String limit;
	public final Integer DEFAULT_OFFSET = 0;
	public final Integer DEFAULT_LIMIT = 20;
	public final Integer MAX_LIMIT = 100;
	
	public void processSymbols() {
		if (filter != null) {
			filter = StringParserUtils.replace(StringParserUtils.replace(filter, "[", ""), "]", "");
			filter = StringParserUtils.replace(filter, LogicOperator.AND.getOperatorAlias(), LogicOperator.AND.getOperator());
			filter = StringParserUtils.replace(filter, LogicOperator.OR.getOperatorAlias(), LogicOperator.OR.getOperator());
			
			parseFilterOperators();
		}

		if (projection != null) {
			projection = StringParserUtils.replace(StringParserUtils.replace(projection, "[", ""), "]", "");
		}
		
		if (sum != null) {
			sum = StringParserUtils.replace(StringParserUtils.replace(sum, "[", ""), "]", "");
		}
		
		if (avg != null) {
			avg = StringParserUtils.replace(StringParserUtils.replace(avg, "[", ""), "]", "");
		}
		
		if (count != null) {
			count = StringParserUtils.replace(StringParserUtils.replace(count, "[", ""), "]", "");
		}
		
		if (countDistinct != null) {
			countDistinct = StringParserUtils.replace(StringParserUtils.replace(countDistinct, "[", ""), "]", "");
		}
		
		
		if (groupBy != null) {
			groupBy = StringParserUtils.replace(StringParserUtils.replace(groupBy, "[", ""), "]", "");
		}

		if (sort != null) {
			sort = StringParserUtils.replace(StringParserUtils.replace(sort, "[", ""), "]", "");
		}
	}

	private void parseFilterOperators() {
		List<FilterOperator> simpleCharOperator = Arrays.asList(new FilterOperator[] {
			FilterOperator.EQ, FilterOperator.GT, FilterOperator.LT
		});
		
		for (FilterOperator filterOperator : FilterOperator.values()) {
			if (!simpleCharOperator.contains(filterOperator)) {
				filter = StringParserUtils.replace(filter, filterOperator.getOperatorCommonAlias(), filterOperator.getParseableOperator());
				filter = StringParserUtils.replace(filter, filterOperator.getOperatorAlias(), filterOperator.getParseableOperator());
			}
		}
		
		for (FilterOperator simpleCharfilterOperator : simpleCharOperator) {
			filter = StringParserUtils.replace(filter, simpleCharfilterOperator.getOperatorCommonAlias(), simpleCharfilterOperator.getParseableOperator());
			filter = StringParserUtils.replace(filter, simpleCharfilterOperator.getOperatorAlias(), simpleCharfilterOperator.getParseableOperator());
		}
	}
	
	public void addAndFilter(String filterName, Object filterValue, FilterOperator filterOperator) {
		if (filter == null || "".equals(filter) || "[]".equals(filter)) {
			filter = new StringBuilder(filterName)
					.append(filterOperator.getParseableOperator())
					.append(filterValue.toString()).toString();
		} else {
			if (FilterOperator.IN.equals(filterOperator) || FilterOperator.OU.equals(filterOperator)) {
				filter = new StringBuilder(StringParserUtils.replace(StringParserUtils.replace(filter, "[", ""), "]", ""))
						.append(LogicOperator.AND.getOperator())
						.append(filterName)
						.append(filterOperator.getParseableOperator())
						.append("(")
						.append(filterValue.toString())
						.append(")").toString();
			} else {
				filter = new StringBuilder(StringParserUtils.replace(StringParserUtils.replace(filter, "[", ""), "]", ""))
						.append(LogicOperator.AND.getOperator())
						.append(filterName)
						.append(filterOperator.getParseableOperator())
						.append(filterValue.toString()).toString();
			}
		}
	}
	
	public void addCountField(List<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String fieldName : fieldNames) {
                addCountField(fieldName);
            }
        }
    }
    
    public void addCountField(String fieldName) {
        StringBuilder countFields = new StringBuilder();
        if (count == null || "".equals(count)) {
            count = countFields.append("[")
                .append(fieldName)
                .append("]")
                .toString();
        
        } else {
            count = countFields.append(StringParserUtils.replace(count, "]", ","))
                .append(fieldName)
                .append("]")
                .toString();
        }
    }
    
    public void addCountDistinctField(List<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String fieldName : fieldNames) {
                addCountDistinctField(fieldName);
            }
        }
    }
    
    public void addCountDistinctField(String fieldName) {
        StringBuilder countDistinctFields = new StringBuilder();
        if (countDistinct == null || "".equals(countDistinct)) {
            countDistinct = countDistinctFields.append("[")
                .append(fieldName)
                .append("]")
                .toString();
        
        } else {
            countDistinct = countDistinctFields.append(StringParserUtils.replace(countDistinct, "]", ","))
                .append(fieldName)
                .append("]")
                .toString();
        }
    }
    
    public void addSumField(List<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String fieldName : fieldNames) {
                addSumField(fieldName);
            }
        }
    }
    
    public void addSumField(String fieldName) {
        StringBuilder sumFields = new StringBuilder();
        if (sum == null || "".equals(sum)) {
            countDistinct = sumFields.append("[")
                .append(fieldName)
                .append("]")
                .toString();
        
        } else {
            sum = sumFields.append(StringParserUtils.replace(sum, "]", ","))
                .append(fieldName)
                .append("]")
                .toString();
        }
    }
    
    public void addAvgField(List<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String fieldName : fieldNames) {
                addAvgField(fieldName);
            }
        }
    }
    
    public void addAvgField(String fieldName) {
        StringBuilder avgFields = new StringBuilder();
        if (avg == null || "".equals(avg)) {
            countDistinct = avgFields.append("[")
                .append(fieldName)
                .append("]")
                .toString();
        
        } else {
            avg = avgFields.append(StringParserUtils.replace(avg, "]", ","))
                .append(fieldName)
                .append("]")
                .toString();
        }
    }
    
    public void addGroupByField(List<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            for (String fieldName : fieldNames) {
                addGroupByField(fieldName);
            }
        }
    }
    
    public void addGroupByField(String fieldName) {
        StringBuilder groupByFields = new StringBuilder();
        if (groupBy == null || "".equals(groupBy)) {
            groupBy = groupByFields.append("[")
                .append(fieldName)
                .append("]")
                .toString();
        
        } else {
            groupBy = groupByFields.append(StringParserUtils.replace(groupBy, "]", ","))
                .append(fieldName)
                .append("]")
                .toString();
        }
    }
    
    public void addSortField(String fieldName, SortOrder sortOrder) {
        StringBuilder sortFields = new StringBuilder();
        if (sort == null || "".equals(sort)) {
            sort = sortFields.append("[")
                .append(fieldName)
                .append("=")
                .append(sortOrder.name())
                .append("]")
                .toString();
        
        } else {
            sort = sortFields.append(StringParserUtils.replace(sort, "]", ","))
                .append(fieldName)
                .append("=")
                .append(sortOrder.name())
                .append("]")
                .toString();
        }
    }

	public void addOrFilter(String filterName, String filterValue, FilterOperator filterOperator) {
		if (filter == null || "".equals(filter) || "[]".equals(filter)) {
			filter = new StringBuilder(filterName)
					.append(filterOperator.getParseableOperator())
					.append(filterValue).toString();
		} else {
			if (FilterOperator.IN.equals(filterOperator) || FilterOperator.OU.equals(filterOperator)) {
				filter = new StringBuilder(StringParserUtils.replace(StringParserUtils.replace(filter, "[", ""), "]", ""))
						.append(LogicOperator.OR.getOperator())
						.append(filterName)
						.append(filterOperator.getParseableOperator())
						.append("(")
						.append(filterValue)
						.append(")").toString();
			} else {
				filter = new StringBuilder(StringParserUtils.replace(StringParserUtils.replace(filter, "[", ""), "]", ""))
						.append(LogicOperator.OR.getOperator())
						.append(filterName)
						.append(filterOperator.getParseableOperator())
						.append(filterValue).toString();
			}
		}
	}
	
	public Boolean hasValidAggregateFunction() {
		return (sum != null && !"".equals(sum)) || 
				(avg != null && !"".equals(avg)) || 
				(count != null && !"".equals(count)) ||
				(countDistinct != null && !"".equals(countDistinct));
	}
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getProjection() {
		return projection;
	}
	
	public List<String> getParsedProjection() {
		return StringParserUtils.splitStringList(projection, ',');
	}
	
	public void setProjection(String projection) {
		this.projection = projection;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getOffset() {
		return offset;
	}
	
	public Integer getFetchOffset() {
		if (offset == null || "".equals(offset)) {
			return DEFAULT_OFFSET;
		}
		
		return Integer.parseInt(offset);
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
	
	public void setOffset(Integer offset) {
		if (offset != null) {
			this.offset = offset.toString();
		}
	}

	public String getLimit() {
		return limit;
	}
	
	public Integer getFetchLimit() {
		if (limit == null || "".equals(limit)) {
			return DEFAULT_LIMIT;
		}
		
		Integer fetchLimit = Integer.parseInt(limit);
		
		return fetchLimit <= MAX_LIMIT ? fetchLimit : MAX_LIMIT;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}
	
	public void setLimit(Integer limit) {
		if (limit != null) {
			this.limit = limit.toString();
		}
	}

	public String getSum() {
		return sum;
	}
	
	public List<String> getParsedSum() {
		return StringParserUtils.splitStringList(sum, ',');
	}

	public void setSum(String sum) {
		this.sum = sum;
	}
	
	public String getAvg() {
		return avg;
	}
	
	public List<String> getParsedAvg() {
		return StringParserUtils.splitStringList(avg, ',');
	}

	public void setAvg(String avg) {
		this.avg = avg;
	}

	public String getGroupBy() {
		return groupBy;
	}
	
	public List<String> getParsedGroupBy() {
		return StringParserUtils.splitStringList(groupBy, ',');
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	
	public String getCount() {
		return count;
	}
	
	public List<String> getParsedCount() {
		return StringParserUtils.splitStringList(count, ',');
	}

	public void setCount(String count) {
		this.count = count;
	}
	
	public String getCountDistinct() {
		return countDistinct;
	}
	
	public List<String> getParsedCountDistinct() {
		return StringParserUtils.splitStringList(countDistinct, ',');
	}

	public void setCountDistinct(String countDistinct) {
		this.countDistinct = countDistinct;
	}


	@Override
	public String toString() {
		return "RequestFilter [filter=" + filter + ", projection=" + projection + ", sum=" + sum + ", avg=" + avg
				+ ", count=" + count + ", countDistinct=" + countDistinct + ", groupBy=" + groupBy + ", sort=" + sort
				+ ", offset=" + offset + ", limit=" + limit + "]";
	}

}