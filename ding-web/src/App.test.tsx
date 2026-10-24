import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import App from './App';
import { StrictMode } from 'react';
import { initView } from 'dingtalk-docs-cool-app';

jest.mock('dingtalk-docs-cool-app', () => ({ initView: jest.fn() }));

const head = [{ id: 'FID', name: 'FID', type: 'text', isPrimary: true }, { id: 'FBillNo', name: '单据编号', type: 'text' }];
const entries = [
  { key: 'FEntity', name: '明细信息', supported: true, fields: [{ id: 'FEntity_FEntryID', name: '明细内码', type: 'text', isPrimary: true }, { id: 'FEntity_FOrgAmount', name: '明细信息·申请金额', type: 'number' }] },
  { key: 'FSecond', name: '其他明细', supported: true, fields: [{ id: 'FSecond_FEntryID', name: '其他内码', type: 'text', isPrimary: true }, { id: 'FSecond_FNote', name: '其他明细·备注', type: 'text' }] },
];
let options = entries;
let schemaFails = false;
let savedSelection: any = {};

beforeEach(() => {
  options = entries;
  schemaFails = false;
  savedSelection = {};
  delete (window as any).Dingdocs;
  (initView as jest.Mock).mockImplementation(({ onError }) => onError(new Error('standalone')));
  global.fetch = jest.fn(async (url: RequestInfo | URL, init?: RequestInit) => {
    let body: any = { code: 200, data: null };
    if (String(url).includes('/voucherConfig/list/')) body.data = [{ id: 1, name: '测试连接', serverUrl: 'https://example.invalid', ...savedSelection }];
    if (String(url).includes('/tableConfig/options')) body.data = [{ formKey: 'ER_ExpenseRequest', formName: '费用申请单' }];
    if (String(url).endsWith('/api/referenceSchema')) body = { code: 0, data: { fields: [
      { id: 'FName', key: 'FName', name: '名称', type: 'text' }, { id: 'FNumber', key: 'FNumber', name: '编码', type: 'text' },
    ] } };
    if (String(url).endsWith('/api/formSchema')) body = schemaFails ? { code: 10005, msg: '元数据失败' } : { code: 0, data: { fields: head, entryOptions: options } };
    if (String(url).endsWith('/api/formSchema') && !schemaFails) {
      const params = JSON.parse(JSON.parse(String(init?.body || '{}')).params || '{}');
      if (params.fieldConfigs?.length) {
        const selected = params.selectedEntryKeys || (params.selectedEntryKey ? [params.selectedEntryKey] : []);
        body.data.fields = [...head, ...options.filter(entry => selected.includes(entry.key)).flatMap(entry => entry.fields)].flatMap(field => {
          const main = params.fieldConfigs.find((config: any) => config.fieldId === field.id && !config.outputFieldId);
          const added = params.fieldConfigs.filter((config: any) => config.fieldId === field.id && config.outputFieldId);
          return [{ ...field, name: main?.alias || field.name }, ...added.map((config: any) => ({ ...field, id: config.outputFieldId, name: config.alias || field.name }))];
        });
      }
    }
    return { ok: true, json: async () => body } as Response;
  });
});

function clickNext() {
  fireEvent.click(screen.getByRole('button', { name: '下一步' }));
  const skip = screen.queryByRole('button', { name: '跳过，保持默认' });
  if (skip) fireEvent.click(skip);
}

async function openEntries() {
  render(<App />);
  fireEvent.click(await screen.findByRole('button', { name: '同步数据' }));
  fireEvent.click(await screen.findByRole('button', { name: '下一步' }));
  await screen.findByText('📋 选择明细表');
}

test('metadata multiple selection, both modes, selected fields and real missing-host error', async () => {
  await openEntries();
  fireEvent.click(screen.getByRole('checkbox', { name: /明细信息/ }));
  expect(screen.getByRole('radio', { name: /只有第一行/ })).toBeChecked();
  fireEvent.click(screen.getByRole('radio', { name: /所有行都包含/ }));
  fireEvent.click(screen.getByRole('checkbox', { name: /其他明细/ }));
  expect(screen.getByRole('checkbox', { name: /明细信息/ })).toBeChecked();
  clickNext();
  expect(screen.getAllByRole('option', { name: '其他明细·备注' }).length).toBeGreaterThan(0);
  expect(screen.getAllByRole('option', { name: '明细信息·申请金额' }).length).toBeGreaterThan(0);
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  expect(await screen.findByText('钉钉环境未初始化')).toBeInTheDocument();
  const save = jest.fn().mockResolvedValue(undefined);
  (window as any).Dingdocs = { base: { host: { saveConfigAndGoNext: save } } };
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining({ selectedEntryKeys: ['FEntity', 'FSecond'], headerFillMode: 'ALL_ROWS', configVersion: 2 })));
});

test('changing entity removes stale filters, going back retains the selected mode', async () => {
  await openEntries();
  fireEvent.click(screen.getByRole('checkbox', { name: /明细信息/ }));
  clickNext();
  fireEvent.focus(screen.getAllByRole('combobox')[0]);
  fireEvent.click(screen.getByRole('listbox').querySelector('[role=option]:last-child')!);
  fireEvent.click(screen.getByRole('button', { name: '返回' }));
  fireEvent.click(screen.getByRole('button', { name: '上一步' }));
  expect(screen.getByRole('checkbox', { name: /明细信息/ })).toBeChecked();
  fireEvent.click(screen.getByRole('checkbox', { name: /明细信息/ }));
  fireEvent.click(screen.getByRole('checkbox', { name: /其他明细/ }));
  clickNext();
  expect(screen.getAllByRole('combobox')[0]).toHaveValue('');
  expect(screen.getByText('已清除不属于当前明细表的筛选和排序条件')).toBeInTheDocument();
});

test('no entries keeps header-only workflow', async () => {
  options = [];
  await openEntries();
  expect(screen.getByText('该表单没有可同步的明细表，将仅同步表头。')).toBeInTheDocument();
  expect(screen.queryByRole('radio', { name: /只有第一行/ })).not.toBeInTheDocument();
  clickNext();
  expect(screen.getAllByRole('option', { name: '单据编号' }).length).toBeGreaterThan(0);
});

test('metadata failure does not advance', async () => {
  schemaFails = true;
  render(<App />);
  fireEvent.click(await screen.findByRole('button', { name: '同步数据' }));
  fireEvent.click(await screen.findByRole('button', { name: '下一步' }));
  await screen.findByText('加载表单字段失败');
  expect(screen.queryByText('📋 选择明细表')).not.toBeInTheDocument();
});


test('saved form, detail mode, grouped filters and sorting survive reopening and submission', async () => {
  savedSelection = {
    selectedFormId: 'ER_ExpenseRequest', selectedFormName: '费用申请单',
    selectedEntryKey: 'FEntity', headerFillMode: 'ALL_ROWS',
    filterConditions: [{ conditions: [{ fieldId: 'FBillNo', operator: '=', value: 'TEST-1' }] },
      { conditions: [{ fieldId: 'FEntity_FOrgAmount', operator: '>', value: '100' }] }],
    sortConfigs: [{ fieldId: 'FEntity_FOrgAmount', order: 'desc' }],
  };
  render(<App />);
  fireEvent.click(await screen.findByRole('button', { name: '同步数据' }));
  expect(await screen.findByLabelText(/选择表单/)).toHaveValue('费用申请单');
  clickNext();
  await screen.findByText('📋 选择明细表');
  expect(screen.getByRole('checkbox', { name: /明细信息/ })).toBeChecked();
  expect(screen.getByRole('radio', { name: /所有行都包含/ })).toBeChecked();
  clickNext();
  const save = jest.fn().mockResolvedValue(undefined);
  (window as any).Dingdocs = { base: { host: { saveConfigAndGoNext: save } } };
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining(savedSelection)));
});

test('saved custom form survives loading options and returning to form selection', async () => {
  savedSelection = { selectedFormId: 'CUSTOM_FORM', selectedFormName: '自定义单据' };
  await openEntries();
  fireEvent.click(screen.getByRole('button', { name: '上一步' }));
  expect(screen.getByDisplayValue('CUSTOM_FORM')).toBeInTheDocument();
  expect(screen.getByDisplayValue('自定义单据')).toBeInTheDocument();
  clickNext();
  await screen.findByText('📋 选择明细表');
});


const existingSync = {
  SERVER_URL: 'https://example.invalid', CID: 'existing-account', USER_NAME: 'existing-user',
  APP_ID: 'existing-app', APP_SECRET: 'test-secret', configVersion: 2,
  selectedFormId: 'ER_ExpenseRequest', selectedFormName: '费用申请单',
  selectedEntryKey: 'FEntity', headerFillMode: 'ALL_ROWS',
  filterConditions: [{ conditions: [{ fieldId: 'FBillNo', operator: '=', value: 'KEEP-ME' }] },
    { conditions: [{ fieldId: 'FEntity_FOrgAmount', operator: '>', value: '123' }] }],
  sortConfigs: [{ fieldId: 'FEntity_FOrgAmount', order: 'desc' }],
  futureOption: 'preserved',
};

function mockDingTalkHost(getSyncConfig: jest.Mock, save: jest.Mock, modern = true) {
  (window as any).Dingdocs = { base: { host: modern
    ? { dataSource: { getSyncConfig, saveConfigAndGoNext: save } }
    : { getSyncConfig, saveConfigAndGoNext: save } } };
  (initView as jest.Mock).mockImplementation(({ onReady }) => onReady());
}

async function advanceRestoredConfig() {
  expect(await screen.findByLabelText(/选择表单/)).toHaveValue('费用申请单');
  clickNext();
  await screen.findByText('📋 选择明细表');
  expect(screen.getByRole('checkbox', { name: /明细信息/ })).toBeChecked();
  expect(screen.getByRole('radio', { name: /所有行都包含/ })).toBeChecked();
  clickNext();
}

test.each([true, false])('DingTalk modify settings restores current sheet and preserves untouched values (modern=%s)', async modern => {
  const read = jest.fn().mockResolvedValue(modern ? existingSync : JSON.stringify(existingSync));
  const save = jest.fn().mockResolvedValue(undefined);
  mockDingTalkHost(read, save, modern);
  const first = render(<App />);
  await advanceRestoredConfig();
  fireEvent.change(screen.getByDisplayValue('KEEP-ME'), { target: { value: 'UPDATED' } });
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledTimes(1));
  const expected = { ...existingSync, filterConditions: [
    { conditions: [{ fieldId: 'FBillNo', operator: '=', value: 'UPDATED' }] }, existingSync.filterConditions[1],
  ] };
  expect(save.mock.calls[0][0]).toEqual(expect.objectContaining(expected));
  // Reopen the plugin, as DingTalk does when modifying settings again.
  first.unmount();
  read.mockResolvedValue(save.mock.calls[0][0]);
  render(<App />);
  await advanceRestoredConfig();
  expect(screen.getByDisplayValue('UPDATED')).toBeInTheDocument();
});

test('editing cannot return above form selection and keeps settings on repeated back navigation', async () => {
  mockDingTalkHost(jest.fn().mockResolvedValue(existingSync), jest.fn());
  render(<App />);
  for (let i = 0; i < 2; i++) {
    await advanceRestoredConfig();
    expect(screen.getByDisplayValue('KEEP-ME')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '返回' }));
  fireEvent.click(screen.getByRole('button', { name: '上一步' }));
    fireEvent.click(screen.getByRole('button', { name: '上一步' }));
    expect(screen.queryByRole('button', { name: '上一步' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '返回列表' })).not.toBeInTheDocument();
    expect(screen.getByLabelText(/选择表单/)).toHaveValue('费用申请单');
  }
  const schemaCall = (global.fetch as jest.Mock).mock.calls.find(([url]) => String(url).endsWith('/api/formSchema'));
  const params = JSON.parse(JSON.parse(schemaCall[1].body).params);
  expect(params).toEqual(expect.objectContaining({ CID: existingSync.CID, APP_SECRET: existingSync.APP_SECRET }));
});

test.each([null, {}, ''])('new sync with no host config keeps connection creation available (%j)', async config => {
  mockDingTalkHost(jest.fn().mockResolvedValue(config), jest.fn());
  render(<App />);
  expect(await screen.findByRole('button', { name: '+ 新增配置' })).toBeInTheDocument();
});

test('failed config read blocks empty submission and retry restores saved settings', async () => {
  const read = jest.fn().mockRejectedValueOnce(new Error('failed')).mockResolvedValue(existingSync);
  mockDingTalkHost(read, jest.fn());
  render(<App />);
  expect(await screen.findByRole('alert')).toHaveTextContent('读取原同步设置失败');
  expect(screen.queryByRole('button', { name: '+ 新增配置' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: '提交配置' })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: '重新读取' }));
  await advanceRestoredConfig();
});


test('StrictMode restores settings when the SDK initializes only once', async () => {
  mockDingTalkHost(jest.fn().mockResolvedValue(existingSync), jest.fn());
  let ready: () => void = () => {};
  (initView as jest.Mock).mockImplementation(({ onReady }) => { ready = onReady; });
  render(<StrictMode><App /></StrictMode>);
  expect(initView).toHaveBeenCalledTimes(1);
  await act(async () => ready());
  await advanceRestoredConfig();
});

test('opening a different sheet reads its own configuration', async () => {
  const read = jest.fn().mockResolvedValue(existingSync);
  mockDingTalkHost(read, jest.fn());
  const first = render(<App />);
  await advanceRestoredConfig();
  first.unmount();
  read.mockResolvedValue({ ...existingSync, selectedEntryKey: '', filterConditions: [], sortConfigs: [] });
  render(<App />);
  await screen.findByLabelText(/选择表单/);
  clickNext();
  await screen.findByText('📋 选择明细表');
  expect(screen.getByRole('checkbox', { name: /不同步明细/ })).toBeChecked();
  clickNext();
  expect(screen.queryByDisplayValue('KEEP-ME')).not.toBeInTheDocument();
});


test('editing schema failure provides retry without exposing earlier steps', async () => {
  schemaFails = true;
  mockDingTalkHost(jest.fn().mockResolvedValue(existingSync), jest.fn());
  render(<App />);
  await screen.findByLabelText(/选择表单/);
  clickNext();
  await screen.findByText('加载表单字段失败');
  expect(screen.queryByRole('button', { name: '返回列表' })).not.toBeInTheDocument();
  schemaFails = false;
  await advanceRestoredConfig();
});


test('text filters offer substring and no-value operators and save empty checks', async () => {
  await openEntries();
  clickNext();
  const fields = screen.getAllByRole('combobox');
  fireEvent.focus(fields[0]);
  fireEvent.click(screen.getByRole('listbox').querySelector('[role=option]:last-child')!);
  expect(screen.getByRole('option', { name: /^包含$/ })).toBeInTheDocument();
  expect(screen.getByRole('option', { name: /^不包含$/ })).toBeInTheDocument();
  fireEvent.change(fields[1], { target: { value: '包含' } });
  fireEvent.change(screen.getByPlaceholderText('筛选值'), { target: { value: 'ABC' } });
  fireEvent.change(fields[1], { target: { value: '为空' } });
  expect(screen.queryByPlaceholderText('筛选值')).not.toBeInTheDocument();
  expect(screen.getByText('无需填写筛选值')).toBeInTheDocument();
  const save = jest.fn().mockResolvedValue(undefined);
  (window as any).Dingdocs = { base: { host: { saveConfigAndGoNext: save } } };
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining({
    filterConditions: [{ conditions: [{ fieldId: 'FBillNo', operator: '为空', value: '' }] }]
  })));
});


test.each([
  [{ code: 0, data: [] }, '服务端尚未返回身份校验结果，请先更新后端'],
  [{ code: 500, msg: '身份认证失败' }, '身份认证失败'],
  [{ code: 0, data: { verified: true } }, '身份与授权校验通过'],
])('homepage verification requires authenticated success: %j', async (loginResponse, message) => {
  savedSelection = { serverUrl: 'https://example.invalid', cid: 'test', userName: 'tester', appId: 'app', appSecret: 'secret' };
  const previousFetch = global.fetch;
  global.fetch = jest.fn(async (url, init) => String(url).endsWith('/api/login')
    ? { ok: true, json: async () => loginResponse } as Response
    : previousFetch(url, init));
  render(<App />);
  fireEvent.click(await screen.findByRole('button', { name: '校验' }));
  expect(await screen.findByText(message)).toBeInTheDocument();
  const loginCall = (global.fetch as jest.Mock).mock.calls.find(([url]) => String(url).endsWith('/api/login'));
  expect(JSON.parse(loginCall![1].body)).toEqual({ SERVER_URL: 'https://example.invalid', CID: 'test', USER_NAME: 'tester', APP_ID: 'app', APP_SECRET: 'secret' });
  if (loginResponse.code !== 0 || ('data' in loginResponse && Array.isArray(loginResponse.data))) expect(screen.queryByText('身份与授权校验通过')).not.toBeInTheDocument();
});


test('filter dropdown accepts text, filters names and codes, selects and restores on blur', async () => {
  await openEntries();
  fireEvent.click(screen.getByRole('checkbox', { name: /明细信息/ }));
  clickNext();
  expect(screen.queryByLabelText('搜索筛选字段')).not.toBeInTheDocument();
  const input = screen.getByRole('combobox', { name: '筛选字段' });
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: '申请' } });
  const list = screen.getByRole('listbox');
  expect(list).toHaveTextContent('明细信息·申请金额');
  expect(list).not.toHaveTextContent('单据编号');
  fireEvent.click(list.querySelector('[role=option]:last-child')!);
  expect(input).toHaveValue('明细信息·申请金额');
  expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: 'fbill' } });
  expect(screen.getByRole('listbox')).toHaveTextContent('单据编号');
  expect(screen.getByRole('listbox')).not.toHaveTextContent('申请金额');
  fireEvent.blur(input);
  expect(input).toHaveValue('明细信息·申请金额');
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: '不存在' } });
  expect(screen.getByText('暂无匹配字段')).toBeInTheDocument();
  fireEvent.keyDown(input, { key: 'Escape' });
  expect(input).toHaveValue('明细信息·申请金额');
  fireEvent.click(input);
  fireEvent.change(input, { target: { value: 'fbill' } });
  fireEvent.keyDown(input, { key: 'ArrowDown' });
  fireEvent.keyDown(input, { key: 'Enter' });
  expect(input).toHaveValue('单据编号');
  expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
});


test('optional field aliases restore, reach filters and save with stable field IDs', async () => {
  savedSelection = { selectedFormId: 'ER_ExpenseRequest', selectedFormName: '费用申请单', fieldConfigs: [{ fieldId: 'FBillNo', alias: '原单号' }] };
  await openEntries();
  fireEvent.click(screen.getByRole('button', { name: '下一步' }));
  expect(screen.getByText('⚙️ 字段配置（可选）')).toBeInTheDocument();
  expect(screen.getByLabelText('单据编号别名')).toHaveValue('原单号');
  fireEvent.change(screen.getByLabelText('单据编号别名'), { target: { value: '凭证编号' } });
  fireEvent.click(screen.getByRole('button', { name: '下一步' }));
  await screen.findByText('🔍 筛选条件');
  expect(screen.getByRole('option', { name: '凭证编号' })).toHaveValue('FBillNo');
  const save = jest.fn().mockResolvedValue(undefined);
  (window as any).Dingdocs = { base: { host: { saveConfigAndGoNext: save } } };
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledWith(expect.objectContaining({ fieldConfigs: [{ fieldId: 'FBillNo', alias: '凭证编号' }] })));
});


test('added reference columns reach filtering and are saved with independent output IDs', async () => {
  options = [{ ...entries[0], fields: [...entries[0].fields, { id: 'FEntity_FMaterial.FName', name: '物料', type: 'text', referenceFormId: 'MATERIAL', referenceKey: 'FMaterial' } as any] }];
  await openEntries();
  fireEvent.click(screen.getByRole('checkbox', { name: /明细信息/ }));
  fireEvent.click(screen.getByRole('button', { name: '下一步' }));
  await screen.findByRole('combobox', { name: '物料第1级引用字段' });
  fireEvent.click(screen.getByRole('button', { name: '＋新增行' }));
  const copy = await screen.findByRole('combobox', { name: '物料（新增1）第1级引用字段' });
  fireEvent.focus(copy);
  fireEvent.change(copy, { target: { value: 'number' } });
  fireEvent.click(screen.getByRole('option', { name: '编码（FNumber）' }));
  fireEvent.change(screen.getByLabelText('物料（新增1）别名'), { target: { value: '物料编号' } });
  fireEvent.click(screen.getByRole('button', { name: '下一步' }));
  await screen.findByText('🔍 筛选条件');
  const column = screen.getByRole('option', { name: '物料编号' }) as HTMLOptionElement;
  expect(column.value).toMatch(/^FEntity_FMaterial.FName__ref_/);
  const save = jest.fn().mockResolvedValue(undefined);
  (window as any).Dingdocs = { base: { host: { saveConfigAndGoNext: save } } };
  fireEvent.click(screen.getByRole('button', { name: '提交配置' }));
  await waitFor(() => expect(save).toHaveBeenCalledTimes(1));
  expect(save.mock.calls[0][0].fieldConfigs).toEqual([expect.objectContaining({
    fieldId: 'FEntity_FMaterial.FName', outputFieldId: column.value, alias: '物料编号', referencePath: ['FNumber'],
  })]);
});
