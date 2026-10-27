import { fieldKind, filterOperators, filterError, scalarValue } from './filterRules';
const field = (type: string) => ({ id: 'F', type });
// 操作符随字段类型变化，不向所有类型开放任意比较。
test('operators follow types rather than sharing every comparison', () => {
  expect(filterOperators(field('text'))).toEqual(['=', '!=', '包含', '不包含', '为空', '不为空']);
  expect(filterOperators(field('number'))).toContain('>=');
  expect(filterOperators(field('date'))).not.toContain('包含');
  expect(filterOperators(field('singleSelect'))).toContain('包含于');
  expect(filterOperators({ ...field('singleSelect'), filterType: 'boolean' })).toEqual(['=', '!=', '为空', '不为空']);
  expect(fieldKind({ ...field('text'), filterType: 'number' })).toBe('number');
});
// 旧单元素数组可恢复，多个值要求用户显式修正。
test('legacy singleton values normalize while multiple values need explicit correction', () => {
  expect(scalarValue(['a'])).toBe('a');
  expect(filterError({ fieldId:'F', operator:'=', value:['a','b'] }, field('text'))).toContain('历史条件');
});
// 零值和否值合法，非法数字日期与失效枚举被拒绝。
test('zero and false remain valid; invalid numbers, dates and stale options are rejected', () => {
  const condition = (value: string | string[], operator = '=') => ({ fieldId: 'F', operator, value });
  expect(filterError(condition('0'), field('number'))).toBe('');
  expect(filterError(condition('-0.125'), field('number'))).toBe('');
  expect(filterError(condition('NaN'), field('number'))).toContain('有效数字');
  expect(filterError(condition('2026-02-30'), field('date'))).toContain('有效日期');
  expect(filterError(condition('this_month'), field('date'))).toBe('');
  expect(filterError(condition('this_month','>'), field('date'))).toContain('有效日期');
  const bool = { ...field('singleSelect'), filterType:'boolean', property:{ choices:[{name:'是',value:'true'},{name:'否',value:'false'}] } };
  expect(filterError(condition('false'), bool)).toBe('');
  expect(filterError(condition('old'), bool)).toContain('失效');
  expect(filterError(condition([], '包含于'), bool)).toContain('至少');
  expect(filterError(condition('', '为空'), bool)).toBe('');
});
