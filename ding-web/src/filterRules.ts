/** 前后端筛选规则共享的最小字段结构。 */
export interface FilterField {
  id: string;
  type: string;
  filterType?: string;
  property?: { choices?: { name: string; value: string }[] };
}
/** 界面筛选状态的字段标识、操作符与取值。 */
export interface FilterValue { fieldId: string; operator: string; value: string | string[] }
/** 空与非空操作符不需要额外输入筛选值。 */
export const isEmptyOperator = (operator: string) => operator === '为空' || operator === '不为空';
export const operatorLabels: Record<string, string> = { '=': '等于', '!=': '不等于', '<>': '不等于', '>': '大于', '>=': '大于等于', '<': '小于', '<=': '小于等于' };
/** 结合元数据筛选类型及枚举选项识别布尔、数字、日期和文本。 */
export const fieldKind = (field?: FilterField): string => {
  if (!field) return 'unknown';
  if (field.filterType === 'boolean') return 'boolean';
  if (field.filterType === 'number' && field.type !== 'singleSelect') return 'number';
  // 部分布尔字段以二选一枚举返回，需要结合选项值识别，不能只看字段类型。
  const choices = field.property?.choices;
  if (field.type === 'singleSelect' && choices?.length === 2 && choices.some(c => c.value === 'true') && choices.some(c => c.value === 'false')) return 'boolean';
  return field.type;
};
/** 仅向当前字段类型提供服务端支持的操作符。 */
export const filterOperators = (field?: FilterField): string[] => {
  const basic = ['=', '!='];
  const empty = ['为空', '不为空'];
  switch (fieldKind(field)) {
    case 'text': return [...basic, '包含', '不包含', ...empty];
    case 'number': case 'date': return [...basic, '>', '>=', '<', '<=', ...empty];
    case 'singleSelect': return [...basic, '包含于', ...empty];
    default: return [...basic, ...empty];
  }
};
/** 兼容历史数组取值，只有单元素数组可还原为标量。 */
export const scalarValue = (value: string | string[]) => Array.isArray(value) ? value.length === 1 ? value[0] : '' : value;
/** 按操作符、字段类型和枚举白名单校验条件，不静默丢弃历史多选条件。 */
export const filterError = (filter: FilterValue, field?: FilterField): string => {
  if (!filter.fieldId) return '';
  if (!field) return '字段已失效，请重新选择';
  if (isEmptyOperator(filter.operator)) return '';
  const kind = fieldKind(field);
  if (filter.operator === '包含于') {
    if (!Array.isArray(filter.value) || !filter.value.length) return '请至少选择一个选项';
    if (!field.property?.choices || filter.value.some(v => !field.property!.choices!.some(c => c.value === v))) return '选项已失效，请重新选择';
    return '';
  }
  // 历史多选等于条件无法安全压成一个值，提示用户显式选择单值或改用包含于。
  if (Array.isArray(filter.value) && filter.value.length > 1) return '此历史条件包含多个值，请选择单个值或改用“包含于”';
  const value = scalarValue(filter.value);
  if (!value.trim()) return kind === 'singleSelect' || kind === 'boolean' ? '请选择一个选项' : '请输入筛选值';
  if (kind === 'number' && (!/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:e[+-]?\d+)?$/i.test(value) || !Number.isFinite(Number(value)))) return '请输入有效数字';
  if (kind === 'singleSelect' || kind === 'boolean') {
    if (!field.property?.choices?.some(c => c.value === value)) return '选项已失效，请重新选择';
  }
  // 日期字符串除格式正确外，还要往返验证真实日期，避免把不存在的日期自动滚入下个月。
  if (kind === 'date') {
    if (filter.operator === '=' && ['last_7_days','last_30_days','this_week','this_month','this_year'].includes(value)) return '';
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || !Number.isFinite(Date.parse(value)) || new Date(value).toISOString().slice(0,10) !== value) return '请选择有效日期';
  }
  return '';
};
