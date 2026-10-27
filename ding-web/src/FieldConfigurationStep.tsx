import { useEffect, useRef, useState } from 'react';
import './FieldConfigurationStep.css';
import ReferenceSearchSelect from './ReferenceSearchSelect';

/** 可配置来源字段，保留引用对象及真实业务键。 */
export interface ConfigurableField {
  id: string;
  name: string;
  type: string;
  referenceFormId?: string;
  referenceKey?: string;
  referenceViaOwner?: boolean;
}
/** 一个输出列的别名与引用路径；新增列通过独立标识与来源区分。 */
export interface FieldConfig {
  fieldId: string;
  outputFieldId?: string;
  alias?: string;
  referencePath?: string[];
  dictionaryVersion?: number;
}
export interface ReferenceField extends ConfigurableField { key: string }
type Loader = (formId: string) => Promise<ReferenceField[]>;
const FIELD_GUIDE_URL = 'https://alidocs.dingtalk.com/i/p/ZdN0G7l984vWzWYKPDmrg74dWVZN3Gxd?rnd=0.04613635645315717';
/** 当前引用层所属对象、候选字段和已选键。 */
interface ReferenceLevel { formId: string; fields: ReferenceField[]; selected: string }

/** 优先沿用有效默认属性，缺失时回退到元数据列表第一项。 */
function defaultReferenceKey(fields: ReferenceField[], preferred?: string) {
  return fields.find(item => item.key === preferred)?.key || fields[0]?.key || '';
}

/** 把当前引用层的元数据转为可搜索选项，引用属性带继续展开标记。 */
function ReferenceValueSelect({ field, level, index, onChange }: {
  field: ConfigurableField; level: ReferenceLevel; index: number; onChange: (index: number, key: string) => void;
}) {
  return <ReferenceSearchSelect label={`${field.name}第${index + 1}级引用字段`} value={level.selected}
    options={[{ value: '', label: '保持默认显示字段' }, ...level.fields.map(item => ({ value: item.key,
      label: `${item.name}${item.referenceFormId ? ' ↗' : ''}（${item.key}）` }))]}
    onChange={value => onChange(index, value)} />;
}

/** 管理一个输出列的别名和多层引用，异步结果只接受最后一次有效请求。 */
function FieldConfigurationRow({ field, config, onChange, loadFields, onAdd, onRemove }: {
  field: ConfigurableField; config?: FieldConfig; onChange: (value: FieldConfig) => void; loadFields: Loader; onAdd: () => void; onRemove?: () => void;
}) {
  const [levels, setLevels] = useState<ReferenceLevel[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  // 请求代数用于丢弃晚返回的旧结果，避免快速切换字段后被旧引用覆盖。
  const generation = useRef(0);
  // 异步加载完成时读取最新配置，保留用户在等待期间输入的别名。
  const currentConfig = useRef(config);
  currentConfig.current = config;

  /** 恢复完整引用路径并逐级加载对象；旧分组合成FID兼容为编码。 */
  const openReference = async (pathOverride?: string[]) => {
    const request = ++generation.current;
    setLoading(true); setError('');
    try {
      let formId: string | undefined = field.referenceFormId;
      const next: ReferenceLevel[] = [];
      // 复制已保存路径，兼容修复与默认回退不直接修改props中的原数组。
      const path = [...(pathOverride ?? currentConfig.current?.referencePath ?? [])];
      let usedFallback = false;
      for (let index = 0; formId && index <= path.length && index < 10; index++) {
        const fields = await loadFields(formId);
        if (request !== generation.current) return;
        const defaultKey = index === 0 ? field.id.split('.').pop() : next[index - 1]?.fields.find(item => item.key === next[index - 1].selected)?.id.split('.').pop();
        // 旧未标版本的分组FID曾是错误合成值，恢复时改为编码；新版本的真实内码选择保持不变。
        if (formId === 'BOS_FORMGROUP' && !currentConfig.current?.dictionaryVersion && path[index] === 'FID') {
          path[index] = 'FNumber';
          usedFallback = true;
        }
        const selected = path[index] || defaultReferenceKey(fields, defaultKey);
        if (!path[index] && selected && selected !== defaultKey) usedFallback = true;
        if (selected && !fields.some(item => item.key === selected)) throw new Error('已保存的引用字段不存在，请恢复默认后重新选择');
        next.push({ formId, fields, selected });
        formId = fields.find(item => item.key === selected)?.referenceFormId;
      }
      if (request === generation.current) {
        setLevels(next);
        // 默认属性不存在或旧分组路径需要迁移时，将实际选择写回配置，不能只改变下拉显示。
        if (usedFallback) onChange({ ...currentConfig.current, fieldId: field.id, referencePath: next.map(level => level.selected).filter(Boolean), dictionaryVersion: 1 });
      }
    } catch (reason) {
      if (request === generation.current) setError(reason instanceof Error ? reason.message : '加载引用对象失败');
    } finally { if (request === generation.current) setLoading(false); }
  };

  useEffect(() => {
    const requestCounter = generation;
    if (field.referenceFormId) void openReference();
    // 组件卸载或来源变化后使旧请求失效，禁止继续回填已不属于本行的元数据。
    return () => { requestCounter.current++; };
    // 别名与选择变化由本行处理，不因每次输入重新拉取引用元数据。
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [field.id, field.referenceFormId, loadFields]);

  /** 提交当前层选择，截断失效子层，并在引用对象存在时加载下一层。 */
  const selectReference = async (index: number, key: string) => {
    const request = ++generation.current;
    // 当前层变更后丢弃所有旧子层，防止提交一条混合新旧对象的路径。
    const next = levels.slice(0, index + 1).map((level, i) => i === index ? { ...level, selected: key } : level);
    const path = next.map(level => level.selected).filter(Boolean);
    onChange({ ...config, fieldId: field.id, alias: config?.alias, referencePath: path, dictionaryVersion: 1 });
    setLevels(next); setError(''); setLoading(false);
    const target = next[index].fields.find(item => item.key === key)?.referenceFormId;
    if (!target || next.length >= 10) return;
    setLoading(true);
    try {
      const fields = await loadFields(target);
      const defaultKey = next[index].fields.find(item => item.key === key)?.id.split('.').pop();
      if (request === generation.current) {
        const selected = defaultReferenceKey(fields, defaultKey);
        setLevels([...next, { formId: target, fields, selected }]);
        if (selected && selected !== defaultKey) {
          const fallbackPath = [...path, selected];
          onChange({ ...currentConfig.current, fieldId: field.id, referencePath: fallbackPath, dictionaryVersion: 1 });
          if (fields.find(item => item.key === selected)?.referenceFormId) await openReference(fallbackPath);
        }
      }
    } catch (reason) {
      if (request === generation.current) setError(reason instanceof Error ? reason.message : '加载下一级引用失败');
    } finally { if (request === generation.current) setLoading(false); }
  };

  return <tbody className="field-config-row-group" aria-label={`${field.name}字段配置`}>
    <tr className="field-config-main-row">
      <th scope="row"><div className="field-config-name"><strong>{field.name}</strong><small>{field.referenceKey || field.id}</small></div>
        {field.referenceFormId && <div className="field-config-row-actions"><button type="button" onClick={onAdd}>＋新增行</button>
          {onRemove && <button type="button" onClick={onRemove}>删除新增行</button>}</div>}</th>
      <td><input className="field-config-control" aria-label={`${field.name}别名`} maxLength={100} placeholder={field.name}
        value={config?.alias || ''} onChange={event => onChange({ ...config, fieldId: field.id, alias: event.target.value })} /></td>
      <td>
        {field.referenceFormId ? <>
          {levels[0] ? <ReferenceValueSelect field={field} level={levels[0]} index={0} onChange={selectReference} />
            : <span className="field-config-muted">{loading ? '正在读取引用字段...' : '引用字段未加载'}</span>}
          {!!config?.referencePath?.length && <button type="button" className="field-config-reset" onClick={() => {
            generation.current++; setLoading(false); setError(''); setLevels([]);
            onChange({ ...config, fieldId: field.id, referencePath: undefined });
            void openReference([]);
          }}>恢复默认显示</button>}
        </> : <span className="field-config-muted">原字段值</span>}
      </td>
    </tr>
    {levels.slice(1).map((level, childIndex) => {
      const index = childIndex + 1;
      const parent = levels[index - 1];
      const parentField = parent.fields.find(item => item.key === parent.selected);
      return <tr className="field-config-child-row" key={`${index}-${level.formId}`}>
        <th scope="row"><div className="field-config-branch" style={{ paddingLeft: `${12 + Math.min(index, 7) * 14}px` }}>
          <span aria-hidden="true">↳</span><div><strong>{parentField?.name || '引用字段'}</strong><small>{level.formId} · 第{index + 1}级</small></div>
        </div></th>
        <td className="field-config-muted"><span title="引用子行沿用主行的显示别名">—</span></td>
        <td><ReferenceValueSelect field={field} level={level} index={index} onChange={selectReference} /></td>
      </tr>;
    })}
    {loading && levels.length > 0 && <tr className="field-config-message-row"><td colSpan={3} role="status">正在获取引用对象元数据...</td></tr>}
    {error && <tr className="field-config-message-row"><td colSpan={3} role="alert">{error} <button type="button" onClick={() => openReference()}>重试</button></td></tr>}
    {!loading && !error && levels[0]?.fields.length === 0 && <tr className="field-config-message-row"><td colSpan={3}>该引用对象没有可选择的显示字段。</td></tr>}
    {!field.referenceFormId && !field.referenceViaOwner && /\.F(Name|Number)$/i.test(field.id) && <tr className="field-config-message-row"><td colSpan={3} className="field-config-metadata-error" role="alert">当前接口未返回引用对象元数据，请更新并重启本地后端，重新选择单据后再配置显示字段。</td></tr>}
  </tbody>;
}

/** 字段配置列表入口，支持搜索、同来源新增列、文档阅读和流程切换。 */
export default function FieldConfigurationStep({ fields, configs, onChange, loadFields, onBack, onNext, busy }: {
  fields: ConfigurableField[]; configs: FieldConfig[]; onChange: (configs: FieldConfig[]) => void;
  loadFields: Loader; onBack: () => void; onNext: () => void; busy: boolean;
}) {
  const [search, setSearch] = useState('');
  // 多行元数据可能同时回填，合并时使用最新配置集合，防止最后一个回填覆盖其他行。
  const latestConfigs = useRef(configs);
  latestConfigs.current = configs;
  const guideDialog = useRef<HTMLDialogElement>(null);
  const [guideOpened, setGuideOpened] = useState(false);
  /** 首次点击时加载在线手册并打开原生对话框，关闭后继续保留配置界面。 */
  const openGuide = () => {
    setGuideOpened(true);
    guideDialog.current?.showModal();
  };
  /** 按输出列标识合并配置，使用最新引用防止并发元数据回填相互覆盖。 */
  const update = (config: FieldConfig) => {
    const remaining = latestConfigs.current.filter(item => (item.outputFieldId || item.fieldId) !== (config.outputFieldId || config.fieldId));
    const next = config.outputFieldId || config.alias?.trim() || config.referencePath?.length ? [...remaining, config] : remaining;
    latestConfigs.current = next;
    onChange(next);
  };
  /** 为同一来源创建独立稳定输出标识及默认别名，不更改金蝶业务数据。 */
  const addRow = (field: ConfigurableField) => {
    // 新增列优先使用随机UUID，后备方案仍提供独立标识；别名可重复但输出标识必须独立。
    const suffix = globalThis.crypto?.randomUUID?.().replace(/-/g, '') || `${Date.now().toString(36)}${Math.random().toString(36).slice(2)}`;
    const copies = configs.filter(item => item.fieldId === field.id && item.outputFieldId).length;
    onChange([...configs, { fieldId: field.id, outputFieldId: `${field.id}__ref_${suffix}`, alias: `${field.name}（新增${copies + 1}）` }]);
  };
  const visible = fields.filter(field => `${field.name} ${field.id}`.toLowerCase().includes(search.trim().toLowerCase()));
  return <div className="field-config-step">
    <div className="field-config-heading">
      <h3>⚙️ 字段配置（可选）</h3>
      <button type="button" className="field-config-guide-link" onClick={openGuide}>📖 操作手册</button>
    </div>
    <dialog ref={guideDialog} className="field-config-guide-dialog" aria-labelledby="field-config-guide-title">
      <div className="field-config-guide-content">
        <div className="field-config-guide-heading">
          <h3 id="field-config-guide-title">字段配置操作手册</h3>
          <button type="button" autoFocus aria-label="关闭操作手册" onClick={() => guideDialog.current?.close()}>✕</button>
        </div>
        <div className="field-config-guide-toolbar">
          <a href={FIELD_GUIDE_URL} target="_blank" rel="noopener noreferrer">在新窗口打开 ↗</a>
          <span>文档未显示时，可在新窗口查看</span>
        </div>
        {guideOpened && <iframe src={FIELD_GUIDE_URL} title="字段配置在线操作手册" className="field-config-guide-frame" />}
      </div>
    </dialog>
    <label className="field-config-label">
      <input aria-label="查找要配置的字段" disabled={busy} placeholder="按字段名称或编码查找" value={search} onChange={event => setSearch(event.target.value)} />
    </label>
    <fieldset className="field-config-list" disabled={busy} aria-busy={busy}>
      <div className="field-config-table-scroll">
        <table className="field-config-table" aria-label="同步字段配置列表">
          <colgroup><col style={{ width: '34%' }} /><col style={{ width: '30%' }} /><col style={{ width: '36%' }} /></colgroup>
          <thead><tr><th scope="col">字段名（金蝶）</th><th scope="col">显示别名（AI 表格）</th><th scope="col">引用显示字段</th></tr></thead>
          {visible.flatMap(field => [
            <FieldConfigurationRow key={field.id} field={field} config={configs.find(item => item.fieldId === field.id && !item.outputFieldId)}
              onChange={update} loadFields={loadFields} onAdd={() => addRow(field)} />,
            ...configs.filter(item => item.fieldId === field.id && item.outputFieldId).map((config, index) =>
              <FieldConfigurationRow key={config.outputFieldId} field={{ ...field, name: `${field.name}（新增${index + 1}）` }} config={config}
                onChange={update} loadFields={loadFields} onAdd={() => addRow(field)}
                onRemove={() => onChange(configs.filter(item => item.outputFieldId !== config.outputFieldId))} />),
          ])}
          {visible.length === 0 && <tbody><tr><td colSpan={3} className="field-config-empty">没有匹配的字段。</td></tr></tbody>}
        </table>
      </div>
    </fieldset>
    <div className="field-config-actions">
      <button type="button" disabled={busy} onClick={onBack}>上一步</button>
      <button type="button" disabled={busy} onClick={onNext}>{busy ? '校验配置中...' : configs.length ? '下一步' : '跳过，保持默认'}</button>
    </div>
  </div>;
}
