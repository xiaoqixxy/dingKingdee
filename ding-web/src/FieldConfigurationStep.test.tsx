import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { useState } from 'react';
import FieldConfigurationStep, { FieldConfig, ReferenceField } from './FieldConfigurationStep';

const source = [{ id: 'Entry_Material.FName', name: '明细·物料', type: 'text', referenceFormId: 'MATERIAL', referenceKey: 'Material' }];
const material: ReferenceField[] = [
  { id: 'FName', key: 'FName', name: '名称', type: 'text' },
  { id: 'FNumber', key: 'FNumber', name: '编码', type: 'text' },
  { id: 'FSpecification', key: 'FSpecification', name: '规格型号', type: 'text' },
  { id: 'FUnit.FName', key: 'FUnit', name: '基本单位', type: 'text', referenceFormId: 'UNIT', referenceKey: 'FUnit' },
];
const unit: ReferenceField[] = [{ id: 'FName', key: 'FName', name: '名称', type: 'text' }];

function Harness({ loadFields, initial = [], fields = source }: { loadFields: (id: string) => Promise<ReferenceField[]>; initial?: FieldConfig[]; fields?: typeof source }) {
  const [configs, setConfigs] = useState(initial);
  return <><FieldConfigurationStep fields={fields} configs={configs} onChange={setConfigs}
    loadFields={loadFields} onBack={jest.fn()} onNext={jest.fn()} busy={false} />
    <output aria-label="saved-config">{JSON.stringify(configs)}</output></>;
}

function chooseReference(label: string, key: string) {
  const input = screen.getByRole('combobox', { name: label });
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: key } });
  fireEvent.click(screen.getByRole('option', { name: new RegExp(`（${key}）$`) }));
}

test('reference dropdown loads from object metadata automatically, defaults to FName and allows nested choices', async () => {
  const load = jest.fn(async id => id === 'MATERIAL' ? material : unit);
  render(<Harness loadFields={load} />);
  expect(load).toHaveBeenCalledWith('MATERIAL');
  expect(await screen.findByLabelText('明细·物料第1级引用字段')).toHaveValue('名称（FName）');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('[]');
  expect(screen.getByRole('button', { name: '跳过，保持默认' })).toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('明细·物料别名'), { target: { value: '规格' } });
  await screen.findByLabelText('明细·物料第1级引用字段');
  chooseReference('明细·物料第1级引用字段', 'FUnit');
  await screen.findByLabelText('明细·物料第2级引用字段');
  chooseReference('明细·物料第2级引用字段', 'FName');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FUnit","FName"]');
  fireEvent.click(screen.getByRole('button', { name: '恢复默认显示' }));
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"alias":"规格"');
  expect(screen.getByLabelText('saved-config')).not.toHaveTextContent('referencePath');
  await waitFor(() => expect(screen.getByLabelText('明细·物料第1级引用字段')).toHaveValue('名称（FName）'));
});

test.each(['FName', 'FNumber'])('missing default %s selects and persists the first metadata field for every row', async preferred => {
  const fields = ['Bill', 'OtherBill'].map(key => ({ id: `${key}.${preferred}`, name: key, type: 'text', referenceFormId: 'BILL', referenceKey: key }));
  const choices: ReferenceField[] = [{ id: 'FBILLID', key: 'FBILLID', name: '内码', type: 'text' }];
  render(<Harness fields={fields} loadFields={async () => choices} />);
  await waitFor(() => expect(screen.getByLabelText('Bill第1级引用字段')).toHaveValue('内码（FBILLID）'));
  await waitFor(() => {
    const saved = JSON.parse(screen.getByLabelText('saved-config').textContent || '[]');
    expect(saved).toHaveLength(2);
    expect(saved.map((item: FieldConfig) => item.referencePath)).toEqual([['FBILLID'], ['FBILLID']]);
  });
  expect(screen.getByRole('button', { name: '下一步' })).toBeInTheDocument();
});

test('nested missing default selects the first child field and preserves aliases', async () => {
  const choices: ReferenceField[] = [{ id: 'FID', key: 'FID', name: '内码', type: 'text' }];
  render(<Harness initial={[{ fieldId: source[0].id, alias: '单位内码' }]} loadFields={async id => id === 'MATERIAL' ? material : choices} />);
  await screen.findByLabelText('明细·物料第1级引用字段');
  chooseReference('明细·物料第1级引用字段', 'FUnit');
  await waitFor(() => expect(screen.getByLabelText('明细·物料第2级引用字段')).toHaveValue('内码（FID）'));
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FUnit","FID"]');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"alias":"单位内码"');
});

test('empty reference metadata stays empty and invalid saved choices still require correction', async () => {
  const view = render(<Harness loadFields={async () => []} />);
  expect(await screen.findByText('该引用对象没有可选择的显示字段。')).toBeInTheDocument();
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('[]');
  view.unmount();
  render(<Harness initial={[{ fieldId: source[0].id, referencePath: ['DeletedField'] }]} loadFields={async () => material} />);
  expect(await screen.findByRole('alert')).toHaveTextContent('已保存的引用字段不存在');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('DeletedField');
});

test('reopening restores nested choices and failed metadata can be retried', async () => {
  const load = jest.fn().mockRejectedValueOnce(new Error('元数据不可用'))
    .mockImplementation(async id => id === 'MATERIAL' ? material : unit);
  render(<Harness loadFields={load} initial={[{ fieldId: source[0].id, alias: '单位名称', referencePath: ['FUnit', 'FName'] }]} />);
  expect(screen.getByLabelText('明细·物料别名')).toHaveValue('单位名称');
  expect(await screen.findByRole('alert')).toHaveTextContent('元数据不可用');
  fireEvent.click(screen.getByRole('button', { name: '重试' }));
  await waitFor(() => expect(screen.getByLabelText('明细·物料第2级引用字段')).toHaveValue('名称（FName）'));
  expect(screen.getByLabelText('明细·物料第1级引用字段')).toHaveValue('基本单位 ↗（FUnit）');
  chooseReference('明细·物料第1级引用字段', 'FSpecification');
  expect(screen.queryByLabelText('明细·物料第2级引用字段')).not.toBeInTheDocument();
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FSpecification"]');
});


test('the first dropdown changes FName to another metadata field without requiring a nested reference', async () => {
  const load = jest.fn(async () => material);
  render(<Harness loadFields={load} />);
  const select = await screen.findByLabelText('明细·物料第1级引用字段');
  expect(select).toHaveValue('名称（FName）');
  chooseReference('明细·物料第1级引用字段', 'FSpecification');
  expect(select).toHaveValue('规格型号（FSpecification）');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FSpecification"]');
  expect(load).toHaveBeenCalledTimes(1);
});


test('reference search filters partial names and codes and does not commit arbitrary text', async () => {
  render(<Harness loadFields={async () => material} />);
  const input = await screen.findByRole('combobox', { name: '明细·物料第1级引用字段' });
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: '规格' } });
  expect(screen.getByRole('option', { name: '规格型号（FSpecification）' })).toBeInTheDocument();
  expect(screen.queryByRole('option', { name: '名称（FName）' })).not.toBeInTheDocument();
  fireEvent.change(input, { target: { value: 'spec' } });
  fireEvent.keyDown(input, { key: 'Enter' });
  expect(input).toHaveValue('规格型号（FSpecification）');
  fireEvent.click(input);
  fireEvent.change(input, { target: { value: '不存在' } });
  expect(screen.getByText('没有匹配的字段')).toBeInTheDocument();
  fireEvent.keyDown(input, { key: 'Escape' });
  expect(input).toHaveValue('规格型号（FSpecification）');
});

test('additional rows keep independent aliases and reference paths and can be removed', async () => {
  render(<Harness loadFields={async () => material} />);
  await screen.findByRole('combobox', { name: '明细·物料第1级引用字段' });
  fireEvent.click(screen.getByRole('button', { name: '＋新增行' }));
  await screen.findByRole('combobox', { name: '明细·物料（新增1）第1级引用字段' });
  chooseReference('明细·物料（新增1）第1级引用字段', 'FNumber');
  fireEvent.change(screen.getByLabelText('明细·物料（新增1）别名'), { target: { value: '物料编号' } });
  expect(screen.getByRole('combobox', { name: '明细·物料第1级引用字段' })).toHaveValue('名称（FName）');
  const configs = JSON.parse(screen.getByLabelText('saved-config').textContent || '[]');
  expect(configs).toHaveLength(1);
  expect(configs[0]).toEqual(expect.objectContaining({ fieldId: source[0].id, alias: '物料编号', referencePath: ['FNumber'] }));
  expect(configs[0].outputFieldId).toMatch(/__ref_/);
  fireEvent.click(screen.getByRole('button', { name: '删除新增行' }));
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('[]');
  expect(screen.queryByRole('combobox', { name: '明细·物料（新增1）第1级引用字段' })).not.toBeInTheDocument();
});

test('group dictionary choices expose named properties and parent references', async () => {
  const group: ReferenceField = { id: 'FMaterialGroup.FNumber', key: 'FMaterialGroup', name: '物料分组', type: 'text', referenceKey: 'FMaterialGroup', referenceFormId: 'BOS_FORMGROUP' };
  const dictionary: ReferenceField[] = [
    { id: 'FNumber', key: 'FNumber', name: '编码', type: 'text' },
    { id: 'FName', key: 'FName', name: '名称', type: 'text' },
    { id: 'FID', key: 'FID', name: '内码', type: 'text' },
    { id: 'FParentId.FNumber', key: 'FParentId', name: '父级分组', type: 'text', referenceKey: 'FParentId', referenceFormId: 'BOS_FORMGROUP' },
  ];
  const load = jest.fn(async id => id === 'MATERIAL' ? [...material, group] : dictionary);
  render(<Harness loadFields={load} />);
  await screen.findByLabelText('明细·物料第1级引用字段');
  chooseReference('明细·物料第1级引用字段', 'FMaterialGroup');
  expect(screen.getByLabelText('明细·物料第1级引用字段')).toHaveValue('物料分组 ↗（FMaterialGroup）');
  await waitFor(() => expect(screen.getByLabelText('明细·物料第2级引用字段')).toHaveValue('编码（FNumber）'));
  chooseReference('明细·物料第2级引用字段', 'FID');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FMaterialGroup","FID"]');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"dictionaryVersion":1');
  chooseReference('明细·物料第2级引用字段', 'FParentId');
  await screen.findByLabelText('明细·物料第3级引用字段');
  chooseReference('明细·物料第3级引用字段', 'FName');
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FMaterialGroup","FParentId","FName"]');
});

test('legacy synthetic group FID restores codes while new dictionary FID remains the real ID', async () => {
  const group: ReferenceField = { id: 'FMaterialGroup.FNumber', key: 'FMaterialGroup', name: '物料分组', type: 'text', referenceKey: 'FMaterialGroup', referenceFormId: 'BOS_FORMGROUP' };
  const dictionary: ReferenceField[] = [{ id: 'FNumber', key: 'FNumber', name: '编码', type: 'text' }, { id: 'FID', key: 'FID', name: '内码', type: 'text' }];
  const load = async (id: string) => id === 'MATERIAL' ? [...material, group] : dictionary;
  const view = render(<Harness loadFields={load} initial={[{ fieldId: source[0].id, referencePath: ['FMaterialGroup', 'FID'] }]} />);
  await waitFor(() => expect(screen.getByLabelText('明细·物料第2级引用字段')).toHaveValue('编码（FNumber）'));
  expect(screen.getByLabelText('saved-config')).toHaveTextContent('"referencePath":["FMaterialGroup","FNumber"]');
  view.unmount();
  render(<Harness loadFields={load} initial={[{ fieldId: source[0].id, dictionaryVersion: 1, referencePath: ['FMaterialGroup', 'FID'] }]} />);
  await waitFor(() => expect(screen.getByLabelText('明细·物料第2级引用字段')).toHaveValue('内码（FID）'));
});
