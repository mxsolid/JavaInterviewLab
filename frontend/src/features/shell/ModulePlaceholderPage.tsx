import { EmptyState } from '../../components/states';
import { PageHeader } from '../../components/ui/PageHeader';
import { SectionCard } from '../../components/ui/SectionCard';

export function ModulePlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <div className="page-stack">
      <PageHeader title={title} description={description} />
      <SectionCard><EmptyState description="当前工作区正在接入真实 API" /></SectionCard>
    </div>
  );
}
